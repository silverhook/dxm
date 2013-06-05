/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.drools.core.FactException;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SitesSettings;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.LanguageCodeConverters;
import org.kie.api.runtime.KieSession;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;
import java.io.*;
import java.security.Principal;
import java.text.ParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for accessing Jahia services in rules.
 *
 * @author toto
 *         Date: 8 janv. 2008
 *         Time: 12:04:29
 */
public class Service extends JahiaService {
    private static Logger logger = LoggerFactory.getLogger(Service.class);
    private static Service instance;

    private TaggingService taggingService;
    private JahiaSitesService sitesService;
    private SchedulerService schedulerService;
    private CacheService cacheService;
    private JahiaUserManagerService userManagerService;
    private JahiaPasswordPolicyService passwordPolicyService;

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void grantRoleToUser(AddedNodeFact node, String user, String role, KieSession drools) {
        try {
            node.getNode().grantRoles("u:" + user, Collections.singleton(role));
            node.getNode().getSession().save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void grantRoleToGroup(AddedNodeFact node, String group, String role, KieSession drools) {
        try {
            node.getNode().grantRoles("g:" + group, Collections.singleton(role));
            node.getNode().getSession().save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void revokeRoleFromEverybody(AddedNodeFact node, String role, KieSession drools) {
        for (String s : node.getNode().getAclEntries().keySet()) {
            try {
                node.getNode().denyRoles(s, Collections.singleton(role));
                node.getNode().getSession().save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void revokeAllPermissions(AddedNodeFact node) {
        try {
            node.getNode().revokeAllRoles();
            node.getNode().getSession().save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setAclInheritanceBreak(AddedNodeFact node, boolean aclInheritanceBreak) {
        try {
            node.getNode().setAclInheritanceBreak(aclInheritanceBreak);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void importNode(AddedNodeFact node, KieSession drools) throws RepositoryException {
        User user = (User) drools.getGlobal("user");
        String uri = node.getPath();
        String name = node.getName();

        StringTokenizer st = new StringTokenizer(name, "_");

        String type = st.nextToken();
        if (type.equals("siteImport")) {
            try {
                logger.info("Import site " + uri);
                //String sitename = st.nextToken() + "_" + st.nextToken();

                if (!user.getJahiaUser().isRoot()) {
                    return;
                }

                ImportExportBaseService.getInstance().importSiteZip(node.getNode());
            } catch (Exception e) {
                logger.error("Error during import of file " + uri, e);
                cacheService.flushAllCaches();
            }

        } else if (name.endsWith(".zip")) {
            try {
                processFileImport(prepareFileImports(node, node.getName()), user.getJahiaUser());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (ServletException e) {
                logger.error(e.getMessage(), e);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        } else if (name.endsWith(".xml")) {
            JCRSessionWrapper session = node.getNode().getSession();
            try {
                session.importXML("/", node.getNode().getFileContent().downloadFile(), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                session.save();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void importXML(final AddedNodeFact targetNode, final String path, KieSession drools)
            throws RepositoryException {
        InputStream is = null;
        try {
            is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
            if (is == null) {
                throw new FileNotFoundException("Unable to locate resource at the specified path: " + path);
            }
            JCRSessionWrapper session = targetNode.getNode().getSession();
            session.importXML(targetNode.getPath(), is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
            session.save();
        } catch (Exception e) {
            logger.error("Error reading content of file " + path, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.info("Content of the file '" + path + "' for target node " + targetNode + " imported successfully");
    }

    private List<Map<Object, Object>> prepareFileImports(AddedNodeFact node, String name) {
        try {
            Properties exportProps = new Properties();
            Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
            ZipInputStream zis = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream());
            ZipEntry z;
            Map<File, String> imports = new HashMap<File, String>();
            List<File> importList = new ArrayList<File>();
            while ((z = zis.getNextEntry()) != null) {
                File i = File.createTempFile("import", ".zip");
                OutputStream os = new BufferedOutputStream(new FileOutputStream(i));
                try {
                    IOUtils.copy(zis, os);
                } finally {
                    IOUtils.closeQuietly(os);
                }

                String n = z.getName();
                if (n.equals("export.properties")) {
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(i), 1024);
                        exportProps.load(is);
                    } finally {
                        IOUtils.closeQuietly(is);
                        FileUtils.deleteQuietly(i);
                    }
                } else if (n.equals("classes.jar")) {
                    FileUtils.deleteQuietly(i);
                } else if (n.equals("site.properties") || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                    // this is a single site import, stop everything and import
                    FileUtils.deleteQuietly(i);
                    for (File file : imports.keySet()) {
                        FileUtils.deleteQuietly(file);
                    }
                    imports.clear();
                    importList.clear();
                    File tempFile = File.createTempFile("import", ".zip");
                    InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
                    OutputStream tos = new BufferedOutputStream(new FileOutputStream(tempFile));
                    try {
                        IOUtils.copy(is, tos);
                    } finally {
                        IOUtils.closeQuietly(is);
                        IOUtils.closeQuietly(tos);
                    }

                    imports.put(tempFile, name);
                    importList.add(tempFile);
                    break;
                } else {
                    imports.put(i, n);
                    importList.add(i);
                }
            }

            List<Map<Object, Object>> importsInfos = new ArrayList<Map<Object, Object>>();
            Map<String, File> importsInfosSorted = new TreeMap<String, File>();
            File users = null;
            File serverPermissions = null;
            for (Iterator<File> iterator = importList.iterator(); iterator.hasNext(); ) {
                File i = iterator.next();
                String fileName = imports.get(i);
                Map<Object, Object> value = prepareSiteImport(i, imports.get(i));
                if (value != null) {
                    importsInfos.add(value);
                    if ("users.xml".equals(fileName)) {
                        users = i;
                    } else if ("serverPermissions.xml".equals(fileName)) {
                        serverPermissions = i;
                    } else {
                        importsInfosSorted.put(fileName, i);
                    }
                }
            }

            List<File> sorted = new LinkedList<File>(importsInfosSorted.values());
            if (serverPermissions != null) {
                sorted.add(0, serverPermissions);
            }
            if (users != null) {
                sorted.add(0, users);
            }
            return importsInfos;
        } catch (IOException e) {
            logger.error("Cannot read import file :" + e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<Map<Object, Object>>();
    }

    private Map<Object, Object> prepareSiteImport(File i, String filename) throws IOException {
        Map<Object, Object> importInfos = new HashMap<Object, Object>();
        importInfos.put("importFile", i);
        importInfos.put("importFileName", filename);
        importInfos.put("selected", Boolean.TRUE);
        if (filename.endsWith(".xml")) {
            importInfos.put("type", "xml");
        } else {
            ZipEntry z;
            ZipInputStream zis2 = new ZipInputStream(new BufferedInputStream(new FileInputStream(i)));
            boolean isSite = false;
            boolean isLegacySite = false;
            try {
                while ((z = zis2.getNextEntry()) != null) {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        zis2.closeEntry();
                        importInfos.putAll(p);
                        importInfos.put("templates", importInfos.containsKey("templatePackageName") ? importInfos.get(
                                "templatePackageName") : "");
                        importInfos.put("oldsitekey", importInfos.get("sitekey"));
                        isSite = true;
                    } else if (z.getName().startsWith("export_")) {
                        isLegacySite = true;
                    }
                }
            } finally {
                IOUtils.closeQuietly(zis2);
            }
            importInfos.put("isSite", Boolean.valueOf(isSite));
            // todo import ga parameters
            if (isSite || isLegacySite) {
                importInfos.put("type", "site");
                if (!importInfos.containsKey("sitekey")) {
                    importInfos.put("sitekey", "");
                    importInfos.put("siteservername", "");
                    importInfos.put("sitetitle", "");
                    importInfos.put("description", "");
                    importInfos.put("mixLanguage", "false");
                    importInfos.put("templates", "");
                    importInfos.put("siteKeyExists", Boolean.TRUE);
                    importInfos.put("siteServerNameExists", Boolean.TRUE);
                } else {
                    try {
                        importInfos.put("siteKeyExists", Boolean.valueOf(
                                sitesService.getSiteByKey(
                                        (String) importInfos.get("sitekey")) != null || "".equals(importInfos.get(
                                        "sitekey"))));
                        importInfos.put("siteServerNameExists", Boolean.valueOf(
                                sitesService.getSite((String) importInfos.get(
                                        "siteservername")) != null || "".equals(importInfos.get("siteservername"))));
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                importInfos.put("type", "files");
            }

        }
        return importInfos;
    }

    private void processFileImport(List<Map<Object, Object>> importsInfos, JahiaUser user)
            throws IOException, ServletException, JahiaException {

        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            if (infos.get("importFileName").equals("users.xml")) {
                ImportExportBaseService.getInstance().importUsers(file);
                break;
            }
        }

        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            if (infos.get("type").equals("files")) {
                try {
                    ImportExportBaseService.getInstance().importSiteZip(file == null ? null : new FileSystemResource(file), null, infos);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (infos.get("type").equals("xml") && (infos.get("importFileName").equals(
                    "serverPermissions.xml") || infos.get("importFileName").equals("users.xml"))) {

            } else if (infos.get("type").equals("site")) {
                // site import
                String tpl = (String) infos.get("templates");
                if ("".equals(tpl)) {
                    tpl = null;
                }
                try {
                    Locale locale = infos.containsKey("defaultLanguage") ? LanguageCodeConverters
                            .languageCodeToLocale((String) infos.get("defaultLanguage")) : settingsBean
                            .getDefaultLocale();
                    sitesService.addSite(user, (String) infos.get(
                            "sitetitle"), (String) infos.get("siteservername"), (String) infos.get("sitekey"), "",
                            locale, tpl,
                            "fileImport", file == null ? null : new FileSystemResource(file),
                            (String) infos.get(
                                    "importFileName"), true,
                            false, (String) infos.get("originatingJahiaRelease"));
                } catch (Exception e) {
                    logger.error("Cannot create site " + infos.get("sitetitle"), e);
                }
            }

        }

    }

    public void incrementProperty(AddedNodeFact node, String propertyName,
                                  KieSession drools) {
        final Node jcrNode = node.getNode();
        try {
            long aLong = 0;
            try {
                final Property property = jcrNode.getProperty(propertyName);
                aLong = property.getLong();
            } catch (PathNotFoundException e) {
                logger.debug("The property to increment " + propertyName + " does not exist yet", e);
            }
            jcrNode.setProperty(propertyName, aLong + 1);
        } catch (RepositoryException e) {
            logger.error("Error during increment of property " + propertyName + " for node " + node, e);
        }
    }

    public void addToProperty(AddedNodeFact node, String propertyName, List<?> value,
                              KieSession drools) {
        final Node jcrNode = node.getNode();
        try {
            long aLong = 0;
            try {
                final Property property = jcrNode.getProperty(propertyName);
                aLong = property.getLong();
            } catch (PathNotFoundException e) {
                logger.debug("The property to increment " + propertyName + " does not exist yet", e);
            }
            jcrNode.setProperty(propertyName, aLong + Long.valueOf((String) value.get(0)));
        } catch (RepositoryException e) {
            logger.error("Error while adding " + value + " to property " + propertyName + " for node " + node, e);
        }
    }

    public void addNewTag(AddedNodeFact node, final String value, KieSession drools) throws RepositoryException {
        String siteKey = node.getPath().startsWith("/sites/") ? StringUtils.substringBefore(node.getPath().substring(7), "/") : null;
        if (siteKey == null) {
            logger.warn("Current site cannot be detected. Skip adding new tag for the node " + node.getPath());
            return;
        }
        taggingService.tag(node.getNode(), value, siteKey, true);
    }

    public void executeRuleLater(AddedNodeFact node, final String propertyName, final String ruleToExecute, KieSession drools)
            throws SchedulerException, RepositoryException {
        final String uuid = node.getNode().getIdentifier();
        final JobDetail jobDetail = BackgroundJob.createJahiaJob("Rule job: " + ruleToExecute + " on node " + uuid, RuleJob.class);
        jobDetail.setName(ruleToExecute + "-" + uuid);
        jobDetail.setGroup(BackgroundJob.getGroupName(RuleJob.class) + "." + ruleToExecute);
        final JobDataMap map = jobDetail.getJobDataMap();
        map.put(RuleJob.JOB_RULE_TO_EXECUTE, ruleToExecute);
        map.put(RuleJob.JOB_NODE_UUID, uuid);
        map.put(RuleJob.JOB_USER, ((User) drools.getGlobal("user")).getName());
        map.put(RuleJob.JOB_WORKSPACE, ((String) drools.getGlobal("workspace")));

        // cancel the scheduled job if exists 
        schedulerService.getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
        try {
            final Property property = node.getNode().getProperty(propertyName);
            // schedule the job
            schedulerService.getScheduler().scheduleJob(jobDetail, getTrigger(node, property.getType() == PropertyType.DATE ? property.getDate().getTime() : property.getString(), jobDetail.getName(), jobDetail.getGroup()));
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void executeActionLater(AddedNodeFact node, final String propertyName, final String actionToExecute, KieSession drools)
            throws SchedulerException, RepositoryException {
        final Property property = node.getNode().hasProperty(propertyName) ? node.getNode().getProperty(propertyName) : null;
        try {
            doScheduleAction(node, actionToExecute, getTrigger(node, property != null ? (property.getType() == PropertyType.DATE ? property.getDate().getTime() : property.getString()) : null, null, null), drools);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void scheduleAction(AddedNodeFact node, final String actionToExecute,
                               final String cronExpression, KieSession drools) throws SchedulerException,
            RepositoryException {
        try {
            doScheduleAction(node, actionToExecute, getTrigger(node, cronExpression, null, null),
                    drools);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void doScheduleAction(AddedNodeFact node, final String actionToExecute,
                                  final Trigger trigger, KieSession drools) throws SchedulerException,
            RepositoryException {
        final String uuid = node.getNode().getIdentifier();
        final JobDetail jobDetail = BackgroundJob.createJahiaJob("Action job: " + actionToExecute
                + " on node " + uuid, ActionJob.class);
        jobDetail.setName(ActionJob.getJobName(actionToExecute, uuid));
        jobDetail.setGroup(ActionJob.getJobGroup(actionToExecute));
        final JobDataMap map = jobDetail.getJobDataMap();
        map.put(ActionJob.JOB_ACTION_TO_EXECUTE, actionToExecute);
        map.put(ActionJob.JOB_NODE_UUID, uuid);
        map.put(ActionJob.JOB_WORKSPACE, ((String) drools.getGlobal("workspace")));
        // cancel the scheduled job if exists
        schedulerService.getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
        if (trigger != null) {
            // schedule the job
            trigger.setName(jobDetail.getName() + "TRIGGER");
            schedulerService.getScheduler().scheduleJob(jobDetail, trigger);
        }
    }

    public void cancelActionExecution(NodeFact node, final String actionToCancel,
                                      KieSession drools) throws RepositoryException, SchedulerException {
        String jobGroup = ActionJob.getJobGroup(actionToCancel);
        String jobName = ActionJob.getJobName(actionToCancel, node.getIdentifier());
        if (schedulerService.getScheduler().deleteJob(jobName, jobGroup)) {
            logger.info("Action job with the name {} and group {} canceled successfully", jobName,
                    jobGroup);
        }
    }

    private Trigger getTrigger(AddedNodeFact node, Object schedule, String jobName, String group)
            throws ParseException, RepositoryException {
        if (schedule == null) {
            return null;
        }

        if (schedule instanceof Date) {
            return new SimpleTrigger(jobName + "TRIGGER", group, (Date) schedule);
        } else {
            return String.valueOf(schedule).length() > 0 ? new CronTrigger(jobName + "TRIGGER", group, String.valueOf(schedule)) : null;
        }
    }

    public void moveSubnodesToSplitFolder(AddedNodeFact n, KieSession drools) throws RepositoryException {
        JCRAutoSplitUtils.applyAutoSplitRulesOnSubnodes(n.getNode());
    }

    public void moveToSplitFolder(AddedNodeFact n, KieSession drools) throws RepositoryException {
        JCRNodeWrapper newNode = JCRAutoSplitUtils.applyAutoSplitRules(n.getNode());
        if (newNode != null) {
            drools.delete(drools.getFactHandle(n));
            drools.insert(new AddedNodeFact(newNode));
        }
    }

    public void enableAutoSplitting(AddedNodeFact n, String splitConfig, String splitFolderNodeType, KieSession drools) throws RepositoryException {
        JCRAutoSplitUtils.enableAutoSplitting(n.getNode(), splitConfig, splitFolderNodeType);
        Map<JCRNodeWrapper, JCRNodeWrapper> modifiedNodes = JCRAutoSplitUtils.applyAutoSplitRulesOnSubnodes(n.getNode());
        for (Map.Entry<JCRNodeWrapper, JCRNodeWrapper> modifiedNodeEntry : modifiedNodes.entrySet()) {
            try {
                drools.delete(drools.getFactHandle(new AddedNodeFact(modifiedNodeEntry.getKey())));
                drools.insert(new AddedNodeFact(modifiedNodeEntry.getValue()));
            } catch (FactException fe) {
                logger.debug("Seems node " + modifiedNodeEntry.getKey() + " was not in working memory, will not insert replacement.");
            }
        }
    }

    public void publishNode(AddedNodeFact node, KieSession drools) throws RepositoryException {
        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node.getNode();
        final JCRSessionWrapper jcrSessionWrapper = nodeWrapper.getSession();
        jcrSessionWrapper.save();
        Set<String> languages = null;
        if (jcrSessionWrapper.getLocale() != null) {
            languages = Collections.singleton(jcrSessionWrapper.getLocale().toString());
        }

        boolean resetUser = false;
        if (JCRSessionFactory.getInstance().getCurrentUser() == null) {
            JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
            resetUser = true;
        }

        try {
            JCRPublicationService.getInstance().publishByMainId(nodeWrapper.getIdentifier(), jcrSessionWrapper.getWorkspace().getName(),
                    Constants.LIVE_WORKSPACE,
                    languages,
                    false, new ArrayList<String>());
        } catch (Exception e) {
            logger.error("Cannot publish node : " + nodeWrapper.getPath(), e);
        } finally {
            if (resetUser) {
                JCRSessionFactory.getInstance().setCurrentUser(null);
            }
        }
    }

    public void startWorkflowOnNode(AddedNodeFact node, String processKey, String provider, KieSession drools) throws RepositoryException {
        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node.getNode();
        try {
            WorkflowService.getInstance().startProcessAsJob(Arrays.asList(nodeWrapper.getIdentifier()), nodeWrapper.getSession(), processKey, provider, new HashMap<String, Object>(), null);
        } catch (SchedulerException e) {
            logger.error("Cannot schedule job ", e);
        }
    }

    public void flushCache(String cacheId, KieSession drools) {
        Cache<?, ?> cache = cacheService.getCache(cacheId);
        if (cache != null) {
            cache.flush();
            logger.info("Cache '" + cacheId + "' flushed.");
        } else {
            logger.warn("No cache found for name '" + cacheId + "'. Skip flushing.");
        }
    }

    public void flushCacheEntry(String cacheId, String cacheEntryKey, KieSession drools) {
        Cache<Object, Object> cache = cacheService.getCache(cacheId);
        if (cache != null) {
            cache.remove(cacheEntryKey);
        } else {
            logger.warn("No cache found for name '" + cacheId + "'. Skip flushing.");
        }
    }

    public void flushAllCaches(KieSession drools) {
        cacheService.flushAllCaches();
        logger.info("All caches flushed.");
    }

    public void storeUserPasswordHistory(String username, KieSession drools) {
        JahiaUser user = userManagerService.lookupUser(username);
        if (user != null) {
            passwordPolicyService.storePasswordHistory(user);
        } else {
            logger.warn("Unlable to lookup user for name: " + username
                    + ". Skip updating user password history.");
        }
    }

    public void deployModule(String moduleName, AddedNodeFact site, KieSession drools) {
        User user = (User) drools.getGlobal("user");
        try {

            JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            ((JahiaTemplateManagerService) managerService).installModule(moduleName, site.getPath(), user.getName());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setUserManagerService(JahiaUserManagerService userMgrService) {
        this.userManagerService = userMgrService;
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }

    public void setPasswordPolicyService(JahiaPasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    public void createPermission(final String path, final String name, final KieSession drools) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper node = session.getNode(path);
                if (!node.hasNode(name)) {
                    node.addNode(name, "jnt:permission");
                }
                session.save();
                return null;
            }
        });
    }

    public void updateDependencies(AddedNodeFact node) throws RepositoryException {
        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().lookupByFileNameAndVersion(node.getNode().getParent().getName(), new ModuleVersion(node.getName()));
        if (pack != null) {
            Value[] dependencies = node.getNode().getProperty("j:dependencies").getValues();
            List<String> depends = new ArrayList<String>();
            for (Value dependency : dependencies) {
                depends.add(dependency.getString());
            }
            if (!depends.equals(pack.getDepends())) {
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().updateDependencies(pack, depends);
//                ServicesRegistry.getInstance().getJahiaTemplateManagerService().regenerateManifest(pack, node.getNode().getSession());
            }
        }
    }

    public void updatePrivileges(NodeFact node) throws RepositoryException {
        final JCRSiteNode site = node.getParent().getNode().getResolveSite();
        String principal = StringUtils.substringAfter(StringUtils.substringAfterLast(node.getPath(), "/"), "_").replaceFirst("_", ":");
        if (principal.startsWith("jcr:read") || principal.startsWith("jcr:write")) {
            principal = StringUtils.substringAfter(principal, "_").replaceFirst("_", ":");
        }
        final String fPrincipal = principal;
        if (site == null) {
            return;
        }

        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final JahiaGroup priv = groupService.lookupGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME);
        Principal p = null;
        if (principal.startsWith("u:")) {
            p = userManagerService.lookupUser(principal.substring(2));
        } else if (principal.length() > 2) {
            p = groupService.lookupGroup(site.getSiteKey(), principal.substring(2));
        }
        if (p != null) {
            boolean needPrivileged = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    QueryManager q = session.getWorkspace().getQueryManager();
                    String sql = "select ace.[j:roles] AS [rep:facet(facet.mincount=1)] from [jnt:ace] as ace where ace.[j:aceType]='GRANT' and ace.[j:principal] = '" + fPrincipal + "' and isdescendantnode(ace, ['" + site.getPath() + "'])";
                    QueryResultWrapper qr = (QueryResultWrapper) q.createQuery(sql, Query.JCR_SQL2).execute();

                    boolean needPrivileged = false;
                    for (FacetField facetField : qr.getFacetFields()) {
                        if (facetField.getValues() != null) {
                            for (Count facetFieldValue : facetField.getValues()) {
                                try {
                                    JCRNodeWrapper roleNode = session.getNode("/roles/" + facetFieldValue.getName());
                                    if (roleNode.hasProperty("j:privilegedAccess") && roleNode.getProperty("j:privilegedAccess").getBoolean()) {
                                        needPrivileged = true;
                                        break;
                                    }
                                } catch (PathNotFoundException e) {
                                    // ignore exception
                                }
                            }
                        }
                    }

                    return needPrivileged;
                }
            });

            if (needPrivileged && !priv.isMember(p)) {
                logger.info(principal + " need privileged access");
                priv.addMember(p);
            } else if (!needPrivileged && priv.isMember(p)) {
                logger.info(principal + " do not need privileged access");
                priv.removeMember(p);
            }
        }
    }

    public void flushGroupCaches() {
        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        groupService.flushCache();
    }

    /**
     * Used to update the JahiaSite associated to the JCRSiteNode
     *
     * @param node node of the site to update
     */

    public void updateSite(AddedNodeFact node) {
        try {
            sitesService.updateSystemSitePermissions((JCRSiteNode) node.getNode(), node.getNode().getSession());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void updateSystemSiteLanguages(AddedNodeFact node, KieSession drools) {
        try {
            JCRSessionWrapper session = node.getNode().getSession();
            if (!node.getName().equals(JahiaSitesService.SYSTEM_SITE_KEY) && sitesService.updateSystemSiteLanguages((JCRSiteNode) node.getNode(), session)) {
                JCRSiteNode siteByKey = sitesService.getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY, session);
                sitesService.updateSystemSitePermissions(siteByKey, session);
                drools.insert(new ChangedPropertyFact(new AddedNodeFact(siteByKey), siteByKey.getProperty(SitesSettings.LANGUAGES)));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void executeActionNow(NodeFact node, final String actionToExecute, KieSession drools)
            throws SchedulerException, RepositoryException {
        final BackgroundAction action = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getBackgroundActions().get(
                actionToExecute);
        if (action != null) {
            if (node instanceof AddedNodeFact) {
                action.executeBackgroundAction(((AddedNodeFact) node).getNode());
            } else {
                action.executeBackgroundAction(node.getParent().getNode());
            }
        }
    }

    public void deleteNodesWithReference(final String nodetype, final String propertyName, final NodeFact node) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryManager q = session.getWorkspace().getQueryManager();
                String sql = "select * from [" + nodetype + "] where [" + propertyName + "] = '" + node.getIdentifier() + "'";
                QueryResult qr = q.createQuery(sql, Query.JCR_SQL2).execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                    next.remove();
                }
                session.save();
                return null;
            }
        });
    }
}
