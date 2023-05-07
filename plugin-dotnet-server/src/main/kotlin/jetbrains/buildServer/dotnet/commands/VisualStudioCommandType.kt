@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.ToolType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for devenv.com /build command.
 */
class VisualStudioCommandType : CommandType() {

    override val name: String = DotnetCommandType.VisualStudio.id

    override val editPage: String = "editVisualStudioParameters.jsp"

    override val viewPage: String = "viewVisualStudioParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        if (properties[DotnetConstants.PARAM_VISUAL_STUDIO_ACTION].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        if (isDocker(parameters)) return@sequence

        var hasRequirements = false
        parameters[DotnetConstants.PARAM_VISUAL_STUDIO_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.VisualStudio) {
                    yield(Requirement("VS${it.version}_Path", null, RequirementType.EXISTS))
                    hasRequirements = true
                }
            }
        }

        if (!hasRequirements) {
            yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS))
        }

        yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
    }
}