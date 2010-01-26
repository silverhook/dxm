<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page import="java.util.*,
                org.jahia.bin.*,
                org.jahia.data.JahiaData" %>
<%@include file="/views/administration/common/taglibs.jsp" %>
<%
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
    String rowStatus = "even";
    String rowClass = "td_lavender";
    int stretcherToOpen   = 1;
%>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
                <jsp:include page="/admin/include/left_menu.jsp">
                    <jsp:param name="mode" value="site"/>
                </jsp:include>
            <div id="content" class="fit">
              <div class="head">
                <div class="object-title">
                  <fmt:message key="${dialogTitle}"/>
                </div>
              </div>
              <div  class="content-item-noborder">
                <logic:messagesPresent>
                  <html:messages id="msg">
                      <p class="errorbold"><bean:write name="msg"/></p>
                  </html:messages>
                </logic:messagesPresent>
                <logic:messagesPresent message="true">
                  <html:messages id="msg" message="true">
                      <p class="blueColor"><bean:write name="msg"/></p>
                  </html:messages>
                </logic:messagesPresent>
            <form name="mainForm" method="get" action='<%=jData.params().composeStrutsUrl("HtmlSettings","")%>'>
                <input type="hidden" name="site" value="${param.site}"/>
                <input type="hidden" name="group" value="htmlsettings"/>
                <input type="hidden" name="method" value="save"/>
                <%@include file="settings.jspf" %>
                <%@include file="toolbars.jspf" %>
            </form>

</div>
</div>
            </td>
          </tr>
          </tbody>
        </table>
        </div>
        <div id="actionBar">
          <span class="dex-PushButton">
             <span class="first-child">
               <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=site")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
             </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
            <a class="ico-ok" href="javascript:document.mainForm.submit()"><fmt:message key="label.save"/></a>
            </span>
          </span>
        </div>
      </div>