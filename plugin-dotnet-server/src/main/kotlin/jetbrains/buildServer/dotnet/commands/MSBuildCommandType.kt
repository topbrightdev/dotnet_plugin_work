package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet MSBuild command.
 */
class MSBuildCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.MSBuild.id

    override val editPage: String
        get() = "editMSBuildParameters.jsp"

    override val viewPage: String
        get() = "viewMSBuildParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>) = buildSequence {
        var shouldBeWindows = false
        var hasRequirement = false

        parameters[DotnetConstants.PARAM_MSBUILD_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.MSBuild) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            shouldBeWindows = true
                            hasRequirement = when (it.bitness) {
                                ToolBitness.x64 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x64_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                ToolBitness.x86 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x86_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                else -> {
                                    yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "MSBuildTools${it.version}\\.0_.+_Path", null, RequirementType.EXISTS))
                                    true
                                }
                            }
                        }
                        ToolPlatform.Mono -> {
                            yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + MonoConstants.CONFIG_NAME, null, RequirementType.EXISTS))
                            hasRequirement = true
                        }
                    }
                }
            }
        }

        if (!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}