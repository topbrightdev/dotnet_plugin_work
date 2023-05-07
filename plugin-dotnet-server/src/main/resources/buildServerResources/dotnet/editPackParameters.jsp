<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>

<script type="text/javascript">
    BS.DotnetParametersForm.paths["pack"] = "Projects";
</script>

<tr class="advancedSetting">
    <th><label for="${params.packConfigKey}">Configuration:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.packConfigKey}" className="longField"/>
            <bs:projectData type="DotnetConfigurations" sourceFieldId="${params.pathsKey}"
                            targetFieldId="${params.packConfigKey}" popupTitle="Select configuration"
                            selectionMode="single"/>
        </div>
        <span class="error" id="error_${params.packConfigKey}"></span>
        <span class="smallNote">Configuration under which to build.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th class="noBorder"><label for="${params.packOutputKey}">Output directory:</label></th>
    <td>
        <div class="position-relative">
            <props:textProperty name="${params.packOutputKey}" className="longField"/>
            <bs:vcsTree fieldId="${params.packOutputKey}" dirsOnly="true"/>
        </div>
        <span class="error" id="error_${params.packOutputKey}"></span>
        <span class="smallNote">Directory where to place outputs.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.packVersionSuffixKey}">Version suffix:</label></th>
    <td>
        <props:textProperty name="${params.packVersionSuffixKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.packVersionSuffixKey}"></span>
        <span class="smallNote">Defines the value for the $(VersionSuffix) property in the project.</span>
    </td>
</tr>

<c:if test="${not empty propertiesBean.properties[params.packNoBuildKey] or
    not empty propertiesBean.properties[params.packServiceableKey]}">
    <tr class="advancedSetting">
        <th>Options:</th>
        <td>
            <props:checkboxProperty name="${params.packNoBuildKey}"/>
            <label for="${params.packNoBuildKey}">Do not the build project before packing</label><br/>
            <props:checkboxProperty name="${params.packServiceableKey}"/>
            <label for="${params.packServiceableKey}">Set the serviceable flag in the package</label>
        </td>
    </tr>
</c:if>