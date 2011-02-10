<%@ page language="java" %>
<%@ page import="java.util.*,
                 org.jahia.data.viewhelper.principal.PrincipalViewHelper,
                 java.security.Principal" %>
<%@ page import="org.jahia.params.*" %>
<%@ page import="org.jahia.services.*" %>
<%@ page import="org.jahia.exceptions.*" %>
<%@ page import="org.jahia.services.usermanager.*" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="engineTitle" class="java.lang.String" scope="request"/>
<jsp:useBean id="javaScriptPath" class="java.lang.String" scope="request"/>

<%
    List<JahiaGroupManagerProvider> providerList     = (List<JahiaGroupManagerProvider>) request.getAttribute( "providerList" );
int stretcherToOpen   = 1;
%>

<!-- Group selection -->
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/selectbox.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/checkbox.js"></script>

<table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr>
      <td valign="top">
            <!-- Search engine view -->
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td colspan="2">
                        <br /><fmt:message key="label.search"/>&nbsp;:
                        <input type="text" name="searchString" size="20"
                            <%
                                String searchString = request.getParameter("searchString");
                                if (searchString != null) {
                                %>value='<%=request.getParameter("searchString")%>'<%
                                }
                                %>onkeydown="if (event.keyCode == 13) javascript:submitForm('search');">
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="label.in"/>&nbsp;:</td>
                    <td>
                        <input type="radio" name="searchIn" value="allProps" checked="checked"
                               onclick="disableCheckBox(properties);">&nbsp;<fmt:message key="label.allProperties"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td valign="top">
                        <input type="radio" name="searchIn" value="properties"
                               onclick="enableCheckbox(properties);">&nbsp;<fmt:message key="label.properties"/>&nbsp;:<br />
                        <!-- This following list is generated by JSP -->
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="groupname" disabled="disabled">&nbsp;<fmt:message key="org.jahia.engines.groupname.label"/><br />
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="description" disabled="disabled">&nbsp;<fmt:message key="label.description"/> (LDAP)<br />
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="members" disabled="disabled">&nbsp;<fmt:message key="members.label"/> (LDAP)<br />

                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="label.on"/>&nbsp;:</td>
                    <td>
                        <input type="radio" name="storedOn" value="everywhere"
                               <%if (providerList.size() > 1) { %> checked="checked" <% } %>
                               onclick="disableCheckBox(providers);">&nbsp;<fmt:message key="label.everyWhere"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="radio" name="storedOn" value="providers"
                               <%if (providerList.size() <= 1) { %> checked="checked" <% } %>
                               onclick="enableCheckbox(providers);">&nbsp;<fmt:message key="label.providers"/>&nbsp;:<br />
<%
                        for (JahiaGroupManagerProvider curProvider : providerList) {
%>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="checkbox" name="providers" value="<%=curProvider.getKey()%>" disabled="disabled"
                                <%if (providerList.size() <= 1) { %> checked="checked" <% } %>>
                            <fmt:message key='<%= "providers." + curProvider.getKey() + ".label"%>'/> (<%=curProvider.getKey()%>)<br />
<%
                        }
%>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td align="right">
                        <br />
                        <span class="dex-PushButton">
                            <span class="first-child">
                            <a class="ico-search" href="javascript:submitForm('search');"><fmt:message key="label.search"/></a>
                          </span>
                        </span>

                    </td>
                </tr>
            </table>
            <!-- end search engine view -->
        </td>
<%
    Integer userNameWidth=new Integer(30);
    request.getSession().setAttribute("userNameWidth",userNameWidth);
%>
        <td align="center">
        <!-- Display group list -->
            <table class="text" border="0">
                <tr>
                    <td>
                        <table class="text" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td>
                                    <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                            href="javascript:sortSelectBox(document.mainForm.selectedGroup, false);"
                                        title="<fmt:message key='org.jahia.admin.users.ManageGroups.altSortBySource.label'/>"><fmt:message key="org.jahia.admin.users.ManageGroups.altSortBySource.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                          href="javascript:sortSelectBox(document.mainForm.selectedGroup, false, /.{7}/);"
                                        title="<fmt:message key='org.jahia.admin.users.ManageGroups.altSortByGroupId.label'/>"><fmt:message key="org.jahia.admin.users.ManageGroups.altSortByGroupId.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                            href='<%= "javascript:sortSelectBox(document.mainForm.selectedGroup, false, /.{" + (userNameWidth.intValue() + 8) + "}/);" %>'
                                        title="<fmt:message key='label.sortByProperty'/>"><fmt:message key="label.sortByProperty"/></a>
                                    </span>
                                </span>
                                 <!-- <a href="#"
                                       onMouseOut="MM_swapImgRestore()"
                                       onMouseOver="MM_swapImage('helpSorting','','<%=request.getContextPath()%><fmt:message key="org.jahia.helpOn.button" />',1)">
                                       <img align="left" name="helpSorting" alt="Help on sorting ACL entries"
                                            onclick="javascript:popupHelp(event, '<%=request.getContextPath()%>/html/help/helpSorting.htm');"
                                            src="<%=request.getContextPath()%><fmt:message key="org.jahia.helpOff.button" />"
                                            width="11" height="11" border="0"></a>-->
                                </td>

                            </tr>
                        </table>
                        <%
                            Set<Principal> resultSet = (Set<Principal>)request.getAttribute( "resultList" );
                            String[] textPattern = {"Principal", "Provider, 6", "Name, "+userNameWidth, "Properties, 20"};
                            PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
                        %>
                        <select ondblclick="javascript:handleKey(event);"
                                <%if (resultSet.size() == 0) {%>disabled="disabled" <%}%>
                                onkeydown="javascript:handleKeyCode(event.keyCode);"
                                 style="width:435px;"  name="selectedGroup" size="25" multiple="multiple">
                            <%
                                if (resultSet.size() == 0) {
                                %><option value="null" selected="selected">
                                -- - -&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp; - <fmt:message key="org.jahia.admin.users.ManageUsers.noUserFound.label"/> -&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;- - --
                                  </option><%
                                } else {
                                    for (Principal p : resultSet) {
                                    %><option value="<%=((JahiaGroup)p).getGroupname()%>" title="<%=((JahiaGroup)p).getGroupname()%>">
                                        <%=principalViewHelper.getPrincipalTextOption(p)%>
                                    </option><%
                            }
                                } %>
                        </select><br />
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>