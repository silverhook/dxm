<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:include template="hidden.header"/>
<c:set var="children" value="${moduleMap.currentList}"/>

<c:if test="${children.size eq -1}">
    <c:set var="count" value="${moduleMap.listTotalSize}"/>
</c:if>
<c:if test="${!(children.size eq -1)}">
    <c:set var="count" value="${children.size}"/>
</c:if>

<c:if test="${count > 0}">
    <c:set value="${functions:randomInt(count)}" var="itemToDisplay"/>
    <c:forEach items="${children}" var="subchild" begin="${itemToDisplay}" end="${itemToDisplay}">
        <template:module node="${subchild}" template="${moduleMap.subNodesView}"
                         editable="true"/>
    </c:forEach>
</c:if>
<c:if test="${renderContext.editMode}">
    <c:if test="${children.size <= 0}">
        <template:module path="*"/>
    </c:if>
</c:if>