/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.taglibs.uicomponents.user;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerProvider;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.taglibs.AbstractJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * <p>Easy tag that generates the HTML listing of JahiaGroups.</p>
 * <p>the tag use the query attribute, if available, to execute the search .By default the query is "*=*" (all).<br/>
 * you can set a list of comma (or space) separated name-value pairs inside the criteria attribute to finely filter
 * the results against your set of criteria.<br>
 * by default, the number of results is limited to 10 first results, the scope to jahiaDB</p>
 *
 * @author dominique (joe) pillot
 * @version $Id: $
 */
@SuppressWarnings("serial")
public class GroupListTag extends AbstractJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(GroupListTag.class);
    private static final JahiaGroupManagerService GroupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();

    private int displaylimit = 10;
    private String query = "*";
    private String scope = "jahia_db";
    private String criteria = null;
    private int memberslimit = 10;
    private String separator = "&nbsp;";
    private boolean membersvisibility = false;
    private String backCall = null;
    private String styleClass = null;
    private boolean viewMembers = false;

    public int doStartTag() throws JspException {
        RenderContext ctx = getRenderContext();
        if (ctx == null) {
            return SKIP_BODY;
        }
        
        //the list of providers
        List<? extends JahiaGroupManagerProvider> thelist = GroupService.getProviderList();//the list of all available providers
        final Set<JahiaGroup> searchResults = new HashSet<JahiaGroup>();
        Properties searchParameters = new Properties();
        StringBuffer sb = new StringBuffer();
        String siteKey = ctx.getSite().getSiteKey();
        if (query == null || query.equalsIgnoreCase("")) query = "*";//default query
        if (query.indexOf("*") == -1) query += "*";

        // search criteria
        searchParameters.setProperty("*", query);
        // add criteria
        if (criteria != null && !criteria.equalsIgnoreCase("")) {
            //boolean propfound=false;
            String delimitor = " ";
            if (getCriteria().trim().indexOf(",") != -1) delimitor = ",";
            StringTokenizer tk = new StringTokenizer(getCriteria().trim(), delimitor);
            while (tk.hasMoreTokens()) {
                String token = tk.nextToken().trim();
                String p = token.substring(0, token.indexOf("="));
                String v = token.substring(token.indexOf("=") + 1);

                if (!v.substring(v.length() - 1, v.length()).equalsIgnoreCase("*")) v += "*";
                logger.debug("adding criteria " + p + " = " + v);
                searchParameters.setProperty(p, v);
            }

        }

        if (thelist.size() == 1 || getScope().equalsIgnoreCase("all") || getScope().equalsIgnoreCase("everywhere")) {
            logger.debug("searching all");
            //we search on the current siteID domain
            searchResults.addAll(GroupService.searchGroups(siteKey, searchParameters));
        } else {
            String delimitor = " ";
            if (getScope().indexOf(",") != -1) delimitor = ",";
            for (Object aThelist : thelist) {
                JahiaGroupManagerProvider curProvider = (JahiaGroupManagerProvider) aThelist;
                StringTokenizer tk = new StringTokenizer(scope.trim(), delimitor);
                while (tk.hasMoreTokens()) {
                    String token = tk.nextToken();
                    if (curProvider.getKey().trim().toLowerCase().indexOf(token.toLowerCase()) != -1) {
                        logger.debug("searching in " + curProvider.getKey());
                        searchResults.addAll(GroupService.searchGroups(curProvider.getKey(), siteKey, searchParameters));
                    }
                }
            }
        }

        int count = 0;

        for (Object searchResult : searchResults) {
            if (count > displaylimit) break;
            JahiaGroup group = (JahiaGroup) searchResult;
            if (getBackCall() != null) {
                sb.append("<a href=\"").append(backCall.replaceAll("groupid", "'" + group.getGroupKey() + "'")).append("\"");
                if (styleClass != null && !"".equals(styleClass.trim())) {
                    sb.append(" class=\"").append(styleClass).append("\"");
                }
                sb.append(">");
            }
            String thename = group.getGroupname();
            sb.append(thename.toUpperCase());
            if (getBackCall() != null) {
                sb.append("</a>");
            }
            if (viewMembers) {
                sb.append("&nbsp;(");
                if (!membersvisibility) {
                    sb.append("<a href=\"javascript:toggleVisibility('");
                    sb.append(thename).append("')\">");
                }
                Set<Principal> mbrs = group.getRecursiveUserMembers();
                sb.append(mbrs.size()).append(")");
                if (!membersvisibility) sb.append("</a>");
                sb.append("&nbsp;");
                sb.append("<div id=\"").append(thename).append("\"");
                if (!membersvisibility) sb.append(" style=\"display: none;\"");
                sb.append(">");
                int mbrcount = 0;
                for (Principal mbr : mbrs) {
                    mbrcount++;
                    if (mbrcount > memberslimit) {
                        sb.append(" ...").append(separator);
                        break;
                    }
                    sb.append("&nbsp;&nbsp;");
                    sb.append(((JahiaUser) mbr).getUsername().toLowerCase()).append(separator);
                }
                sb.append("</div>");
            }
            sb.append("<br/>");

            count++;
        }
        try {
            pageContext.getOut().println(sb.toString());
        } catch (IOException e) {
            logger.error("Error while rendering the group list tag", e);
        }
        return SKIP_BODY;
    }

    // ACCESSORS
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        logger.debug("setting query to " + query);
        this.query = query;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        logger.debug("setting scope to " + scope);
        this.scope = scope;
    }

    public int getDisplayLimit() {
        return displaylimit;
    }

    public void setDisplayLimit(int limit) {
        logger.debug("setting limit to " + limit);
        this.displaylimit = limit;
    }

    public int getMembersLimit() {
        return memberslimit;
    }

    public void setMembersLimit(int limit) {
        logger.debug("setting memberslimit to " + limit);
        this.memberslimit = limit;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        logger.debug("setting criteria to " + criteria);
        this.criteria = criteria;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        logger.debug("setting separator to " + separator);
        this.separator = separator;
    }

    public boolean getMembersVisibility() {
        return membersvisibility;
    }

    public void setMembersVisibility(boolean visibility) {
        logger.debug("setting members visibility to " + visibility);
        this.membersvisibility = visibility;
    }

    public boolean getViewMembers() {
        return viewMembers;
    }
    public void setViewMembers(boolean viewMembers) {
        this.viewMembers = viewMembers;
    }

    public String getBackCall() {
        return backCall;
    }

    public void setBackCall(String backCall) {
        this.backCall = backCall;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}

