@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

abstract class DotnetType : CommandType() {
    override fun getRequirements(parameters: Map<String, String>) = buildSequence {
        if (isDocker(parameters)) return@buildSequence

        yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
    }
}