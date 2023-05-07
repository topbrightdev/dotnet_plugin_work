package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetCommonArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _msbuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider)
    : DotnetCommonArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    yield(CommandLineArgument("--verbosity"))
                    yield(CommandLineArgument(it.id))
                }
            }

            yieldAll(_msbuildLoggerArgumentsProvider.arguments)
            yieldAll(_customArgumentsProvider.arguments)
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}