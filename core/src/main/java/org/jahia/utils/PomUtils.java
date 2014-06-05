/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import org.apache.commons.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.tika.io.IOUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for reading and manipulating Maven project files (pom.xml).
 * 
 * @author Sergiy Shyrkov
 */
public final class PomUtils {

    private static final Logger logger = LoggerFactory.getLogger(PomUtils.class);

    /**
     * Gets the artifact group ID from the provided model. If the artifact group ID is not specified, takes the group ID of the parent, if
     * present. Otherwise returns <code>null</code>.
     *
     * @param model
     *            the Maven project model
     * @return the artifact group ID from the provided model. If the artifact group ID is not specified, takes the group ID of the parent,
     *         if present. Otherwise returns <code>null</code>
     */
    public static String getGroupId(Model model) {
        String groupId = model.getGroupId();
        if (groupId == null && model.getParent() != null) {
            groupId = model.getParent().getGroupId();
        }

        return groupId;
    }

    /**
     * Reads the artifact version from the provided pom.xml file. If the artifact version is not specified, takes the version of the parent,
     * if present. Otherwise returns <code>null</code>.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to read
     * @return the artifact version from the provided pom.xml file. If the artifact version is not specified, takes the version of the
     *         parent, if present. Otherwise returns <code>null</code>
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static String getVersion(File pomXmlFile) throws IOException, XmlPullParserException {
        return getVersion(read(pomXmlFile));
    }

    /**
     * Gets the artifact version from the provided model. If the artifact version is not specified, takes the version of the parent, if
     * present. Otherwise returns <code>null</code>.
     * 
     * @param model
     *            the Maven project model
     * @return the artifact version from the provided model. If the artifact version is not specified, takes the version of the parent, if
     *         present. Otherwise returns <code>null</code>
     */
    public static String getVersion(Model model) {
        String version = model.getVersion();
        if (version == null && model.getParent() != null) {
            version = model.getParent().getVersion();
        }

        return version;
    }

    /**
     * Parses the Maven project model from the specified pom.xml file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor to read model from
     * @return the Maven project model from the specified pom.xml file
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static Model read(File pomXmlFile) throws IOException, XmlPullParserException {
        Reader reader = new FileReader(pomXmlFile);
        try {
            return new MavenXpp3Reader().read(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Updates the distribution management repository in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param repositoryId
     *            the server ID for the distribution repository
     * @param repositoryUrl
     *            the URL of the target distribution repository
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateDistributionManagement(File pomXmlFile, String repositoryId, String repositoryUrl)
            throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        DistributionManagement dm = model.getDistributionManagement();
        if (dm == null) {
            dm = new DistributionManagement();
            model.setDistributionManagement(dm);
        }
        DeploymentRepository repo = dm.getRepository();
        if (repo == null) {
            repo = new DeploymentRepository();
            dm.setRepository(repo);
        }
        repo.setId(repositoryId);
        repo.setUrl(repositoryUrl);

        write(model, pomXmlFile);
    }

    public static void updateForgeUrl(File pomXmlFile, String forgeUrl)
            throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        model.getProperties().put("jahia-private-app-store", forgeUrl);
        write(model, pomXmlFile);
    }

    /**
     * Updates the Jahia-Depends information in the maven-bundle-plugin to reflect the new list of dependencies.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param depends
     *            comma-separated list of dependencies
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateJahiaDepends(File pomXmlFile, String depends) throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        Build build = model.getBuild();
        if (build == null) {
            build = new Build();
            model.setBuild(build);
        }
        Map<String, Plugin> pluginsAsMap = build.getPluginsAsMap();
        Plugin plugin = pluginsAsMap.get("org.apache.felix:maven-bundle-plugin");
        if (plugin == null) {
            plugin = new Plugin();
            plugin.setGroupId("org.apache.felix");
            plugin.setArtifactId("maven-bundle-plugin");
            plugin.setExtensions(true);
            build.addPlugin(plugin);
        }

        Xpp3Dom cfg = (Xpp3Dom) plugin.getConfiguration();
        if (cfg == null) {
            plugin.setConfiguration(Xpp3DomBuilder.build(new StringReader(
                    "<configuration><instructions><Jahia-Depends>" + depends
                            + "</Jahia-Depends></instructions></configuration>")));
        } else {
            Xpp3Dom instructions = cfg.getChild("instructions");
            if (instructions == null) {
                cfg.addChild(Xpp3DomBuilder.build(new StringReader("<instructions><Jahia-Depends>" + depends
                        + "</Jahia-Depends></instructions>")));
            } else {
                Xpp3Dom jahiaDepends = instructions.getChild("Jahia-Depends");
                if (jahiaDepends == null) {
                    instructions.addChild(Xpp3DomBuilder.build(new StringReader("<Jahia-Depends>" + depends
                            + "</Jahia-Depends>")));
                } else {
                    jahiaDepends.setValue(depends);
                }
            }
        }

        write(model, pomXmlFile);
    }

    /**
     * Updates the SCM connection URL in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param scmUrl
     *            the SCM connection URL to set
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateScm(File pomXmlFile, String scmUrl) throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        Scm scm = model.getScm();
        if (scm == null) {
            scm = new Scm();
            model.setScm(scm);
        }
        scm.setConnection(scmUrl);
        scm.setDeveloperConnection(scmUrl);

        write(model, pomXmlFile);
    }

    /**
     * Updates the project version in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param version
     *            the new version to set
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateVersion(File pomXmlFile, String version) throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        model.setVersion(version);
        write(model, pomXmlFile);
    }

    /**
     * Serializes Maven project model into a specified file.
     * 
     * @param model
     *            the Maven project model to serialize
     * @param targetPomXmlFile
     *            the target file to write the provided model into
     * @throws IOException
     *             in case of a serialization error
     */
    public static void write(Model model, File targetPomXmlFile) throws IOException {
        String copyright = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(targetPomXmlFile);
            Node firstChild = doc.getFirstChild();
            if (firstChild.getNodeType() == Node.COMMENT_NODE) {
                copyright = firstChild.getTextContent();
            }
        } catch (Exception e) {
            logger.warn("Failed to read pom.xml copyright", e);
        }

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        OutputStream os = null;
        String pomContent;
        try {
            os = new ByteArrayOutputStream();
            xpp3Writer.write(os, model);
            pomContent = os.toString();
        } finally {
            IOUtils.closeQuietly(os);
        }

        if (copyright != null) {
            int i = pomContent.indexOf("<project");
            pomContent = pomContent.substring(0, i) + "<!--" + copyright + "-->\n" + pomContent.substring(i);
        }

        org.apache.commons.io.FileUtils.write(targetPomXmlFile, pomContent);
    }

    public static File extractPomFromJar(JarFile jar, String groupId, String artifactId) throws IOException {
        // deploy artifacts to Maven distribution server
        Enumeration<JarEntry> jarEntries = jar.entries();
        JarEntry jarEntry = null;
        boolean found = false;
        while (jarEntries.hasMoreElements()) {
            jarEntry = jarEntries.nextElement();
            String name = jarEntry.getName();
            if (StringUtils.startsWith(name, groupId != null ? ("META-INF/maven/" + groupId + "/") : "META-INF/maven/")
                    && StringUtils.endsWith(name, artifactId + "/pom.xml")) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IOException("unable to find pom.xml file within while looking for " + artifactId);
        }
        InputStream is = jar.getInputStream(jarEntry);
        File pomFile = File.createTempFile("pom", ".xml");
        FileUtils.copyInputStreamToFile(is, pomFile);
        return pomFile;
    }

    private PomUtils() {
        super();
    }

}
