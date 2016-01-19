<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dnx.DnuParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<props:viewWorkingDirectory/>

<c:forEach items="${params.types}" var="type">
    <c:if test="${propertiesBean.properties[params.commandKey] eq type.name}">
        <div class="parameter">
            Command: <strong><c:out value="${type.name}"/></strong>
        </div>

        <jsp:include page="${teamcityPluginResourcesPath}/${type.viewPage}"/>
    </c:if>
</c:forEach>

<c:if test="${not empty propertiesBean.properties[params.argumentsKey]}">
    <div class="parameter">
        Command line parameters: <props:displayValue name="${params.argumentsKey}"/>
    </div>
</c:if>
