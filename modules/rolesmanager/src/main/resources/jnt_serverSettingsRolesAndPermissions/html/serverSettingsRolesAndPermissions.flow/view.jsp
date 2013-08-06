<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jcr:node var="sites" path="/sites"/>
<jcr:nodeProperty name="j:defaultSite" node="${sites}" var="defaultSite"/>
<c:set var="defaultPrepackagedSite" value="acmespace.zip"/>
<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrap-filestyle.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<jsp:useBean id="nowDate" class="java.util.Date"/>
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>
<div class="box-1">
    <fieldset>
        <h2>Roles and permissions</h2>
        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST" >
        <h3>Add role :</h3>
        <select name="roleScope">
            <c:forEach items="${handler.roleTypes.values}" var="roleType">
                <option value="${roleType.name}">
                <fmt:message key="rolesmanager.rolesAndPermissions.roleType.${roleType.name}"/>
                </option>
            </c:forEach>
        </select>
        <input type="text" id="addRoleField" name="newRole"/>
        <button class="btn btn-primary" type="submit" name="_eventId_addRole" >
            <i class="icon-plus  icon-white"></i>
            Add
        </button>
        </form>

    </fieldset>
</div>
<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="alert ${msg.severity == 'ERROR' ? 'validationError' : ''} ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
            ${fn:escapeXml(msg.text)}
    </div>
</c:forEach>

<c:forEach items="${roles}" var="entry" varStatus="loopStatus">
    <fieldset>

           <h3> <fmt:message key="rolesmanager.rolesAndPermissions.roleType.${entry.key}"/></h3>

        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="3%">&nbsp;</th>
                <th width="90%">
                    <fmt:message key="label.name"/>
                </th>
                <%--<th width="7%">--%>
                    <%--Scope--%>
                <%--</th>--%>
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${entry.value}" var="role" varStatus="loopStatus">
                <tr>
                    <td><input name="selectedSites" type="checkbox" value="${role.name}"/></td>
                    <td>
                        <strong><a href="#" onclick="viewRole('${role.uuid}')">${role.name}</a></strong>
                    </td>
                    <%--<td>--%>
                            <%--${role.scope} &nbsp; ${role.privileged ? '' : 'LIVE'}--%>
                    <%--</td>--%>
                </tr>
            </c:forEach>
            </tbody>
        </table>

    </fieldset>
</c:forEach>


<script type="text/javascript">
    function viewRole(uuid) {
        $('#uuid').val(uuid)
        $('#viewRole').submit();
    }
</script>

<form style="margin: 0;" action="${flowExecutionUrl}" method="POST" id="viewRole">
    <input type="hidden" name="uuid" value="" id="uuid"/>
    <input type="hidden" name="_eventId_viewRole" value="on">
</form>


