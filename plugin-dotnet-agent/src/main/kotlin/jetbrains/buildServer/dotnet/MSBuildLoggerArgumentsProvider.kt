package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class MSBuildLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver,
        private val _loggerParameters: LoggerParameters)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("/noconsolelogger"))
        val verbosityStr = _loggerParameters.msBuildLoggerVerbosity?.let { ";verbosity=${it.id.toLowerCase()}" } ?: ""
        yield(CommandLineArgument("/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${_loggerResolver.resolve(ToolType.MSBuild).absolutePath};TeamCity$verbosityStr"))
    }
}