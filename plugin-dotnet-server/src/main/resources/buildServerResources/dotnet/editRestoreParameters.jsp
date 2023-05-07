<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
    BS.DotnetParametersForm.appendProjectFile.push("restore");
    BS.DotnetParametersForm.pathName["restore"] = "Projects";
    BS.DotnetParametersForm.pathHint["restore"] = "Specify paths to projects and solutions";
    BS.DotnetParametersForm.hideWorkingDirectory["restore"] = true;
</script>

<tr class="advancedSetting">
    <th><label for="${params.restoreSourceKey}">NuGet package sources:</label></th>
    <td>
        <c:set var="note">
            Specifies NuGet package sources to use during the restore.<br/>
            For the built-in TeamCity NuGet server use <em>%teamcity.nuget.feed.server%</em>.
        </c:set>
        <props:multilineProperty name="${params.restoreSourceKey}" className="longField" expanded="true"
                                 cols="60" rows="3" linkTitle="Sources" note="${note}"/>
        <span class="error" id="error_${params.restoreSourceKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.restoreRuntimeKey}">Runtime:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.restoreRuntimeKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.restoreRuntimeKey}" popupTitle="Select runtime"/>
        </div>
        <span class="error" id="error_${params.restoreRuntimeKey}"></span>
        <span class="smallNote">Target runtime to restore packages.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.restorePackagesKey}">Packages path:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.restorePackagesKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.restorePackagesKey}" dirsOnly="true"/>
        </div>
        <span class="error" id="error_${params.restorePackagesKey}"></span>
        <span class="smallNote">Directory to install packages in.</span>
    </td>
</tr>

<c:if test="${not empty propertiesBean.properties[params.restoreParallelKey] or
    not empty propertiesBean.properties[params.restoreRootProjectKey] or
    not empty propertiesBean.properties[params.restoreNoCacheKey] or
    not empty propertiesBean.properties[params.restoreIgnoreFailedKey]}">
    <tr class="advancedSetting">
        <th class="noBorder"></th>
        <td class="noBorder">
            <props:checkboxProperty name="${params.restoreParallelKey}"/>
            <label for="${params.restoreParallelKey}">Disables restoring multiple project packages in parallel</label><br/>
            <props:checkboxProperty name="${params.restoreRootProjectKey}"/>
            <label for="${params.restoreRootProjectKey}">Restore only root project packages</label><br/>
            <props:checkboxProperty name="${params.restoreNoCacheKey}"/>
            <label for="${params.restoreNoCacheKey}">Do not cache packages and http requests</label><br/>
            <props:checkboxProperty name="${params.restoreIgnoreFailedKey}"/>
            <label for="${params.restoreIgnoreFailedKey}">Treat package source failures as warnings</label>
        </td>
    </tr>
</c:if>

<tr class="advancedSetting">
    <th><label for="${params.restoreConfigKey}">Configuration file:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.restoreConfigKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.restoreConfigKey}"/>
        </div>
        <span class="error" id="error_${params.restoreConfigKey}"></span>
        <span class="smallNote">The NuGet configuration file to use.</span>
    </td>
</tr>