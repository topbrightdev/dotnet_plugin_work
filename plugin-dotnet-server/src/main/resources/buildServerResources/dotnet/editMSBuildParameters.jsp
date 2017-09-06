<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.appendProjectFile.push("msbuild");
    BS.DotnetParametersForm.paths["msbuild"] = "Projects";
</script>

<tr>
    <th><label for="${params.msbuildTargetsKey}">Targets:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.msbuildTargetsKey}" className="longField"/>
            <bs:projectData type="MSBuildTargets" sourceFieldId="${params.msbuildTargetsKey}" targetFieldId="${params.msbuildTargetsKey}" popupTitle="Select targets to invoke"/>
        </div
        <span class="error" id="error_${params.msbuildTargetsKey}"></span>
        <span class="smallNote">Enter targets separated by space or semicolon.</span></td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.msbuildConfigKey}">Configuration:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.msbuildConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.msbuildConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.msbuildConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.msbuildPlatformKey}">Platform:</label></th>
    <td>
        <div class="posRel">
            <props:textProperty name="${params.msbuildPlatformKey}" className="longField"/>
            <bs:projectData type="DotnetRuntimes" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.msbuildPlatformKey}" popupTitle="Select platform"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.msbuildPlatformKey}"></span>
        <span class="smallNote">Platform under which to build.</span>
    </td>
</tr>