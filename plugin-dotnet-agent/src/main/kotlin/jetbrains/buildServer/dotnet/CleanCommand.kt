package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class CleanCommand(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Clean

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val specificArguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_CLEAN_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--framework"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_CLEAN_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_CLEAN_RUNTIME)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--runtime"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_CLEAN_OUTPUT)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
                    yield(CommandLineArgument(it))
                }
            }

            yieldAll(_commonArgumentsProvider.arguments)
        }

    override fun isSuccess(exitCode: Int): Boolean = exitCode == 0

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}