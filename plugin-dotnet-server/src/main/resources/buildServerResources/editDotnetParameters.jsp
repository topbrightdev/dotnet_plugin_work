<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.dotnet.DotnetParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<tr>
    <th><label for="${params.integrationPackagePathKey}">Integration package: </label></th>
    <td>
        <jsp:include page="/tools/selector.html?toolType=${params.integrationPackageToolTypeKey}&versionParameterName=${params.integrationPackagePathKey}&class=${clazz}"/>
    </td>
</tr>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}dotnet-settings.css'/>");

    BS.DotnetParametersForm = {
        appendProjectFile: [],
        selectProjectFile: function (chosenFile) {
            var $paths = $j('#${params.pathsKey}');
            var value = BS.Util.trimSpaces($paths.val());
            var commandName = $j('#${params.commandKey}').val();
            var appendFile = BS.DotnetParametersForm.appendProjectFile.indexOf(commandName) >= 0;
            chosenFile = chosenFile.indexOf(" ") >= 0 ? '"' + chosenFile + '"' : chosenFile;
            $paths.val(appendFile && value.length > 0 ? value + " " + chosenFile : chosenFile);
        },
        paths: []
    };

    $j(document).on('change', '#${params.commandKey}', function () {
        var command = $j(this).val();
        var pathsName = BS.DotnetParametersForm.paths[command];
        var pathsRow = $j("#${params.pathsKey}-row");
        if (pathsName) {
            pathsRow.show().find("label").text(pathsName + ':');
        } else {
            pathsRow.hide();
        }

        $j(".runnerFormTable span.error").empty();
    });

    $j(document).on('ready', '#${params.commandKey}', function () {
        $j(this).change();
    });
</script>

<props:selectSectionProperty name="${params.commandKey}" title="Command:" note="">
    <tr class="advancedSetting" id="${params.pathsKey}-row">
        <th class="noBorder"><label for="${params.pathsKey}">Projects:</label></th>
        <td>
            <div class="posRel clearfix">
                <div class="dnx left">
                    <props:textProperty name="${params.pathsKey}" className="longField" expandable="true"/>
                </div>
                <bs:vcsTree treeId="${params.pathsKey}" callback="BS.DotnetParametersForm.selectProjectFile"/>
            </div>
            <span class="error" id="error_${params.pathsKey}"></span>
            <span class="smallNote">Enter targets separated by space or new line.</span></td>
        </td>
    </tr>

    <props:workingDirectory/>

    <c:forEach items="${params.types}" var="type">
        <props:selectSectionPropertyContent value="${type.name}" caption="${type.name}">
            <jsp:include page="${teamcityPluginResourcesPath}/dotnet/${type.editPage}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters to dotnet.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosity}">
                <props:option value="${item}"><c:out value="${item}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosityKey}"></span>
    </td>
</tr>
