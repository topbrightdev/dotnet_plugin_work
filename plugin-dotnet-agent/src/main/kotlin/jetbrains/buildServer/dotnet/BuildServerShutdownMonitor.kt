package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import org.apache.log4j.Logger
import java.io.File

class BuildServerShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _parametersService: ParametersService,
        private val _environmentVariables: EnvironmentVariables)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _workingDirectories = mutableMapOf<Version, File>()

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            if (_workingDirectories.isNotEmpty()) {
                try {
                    LOG.debug("Shared compilation service shutdown.")
                    val executableFile = _dotnetToolResolver.executableFile
                    for ((sdkVersion, workingDirectory) in _workingDirectories) {
                        val envVariables = _environmentVariables.getVariables(sdkVersion).toList()
                        _commandLineExecutor.tryExecute(
                                CommandLine(
                                        TargetType.Tool,
                                        executableFile,
                                        workingDirectory,
                                        shutdownArgs,
                                        envVariables)
                        )
                    }
                } finally {
                    _workingDirectories.clear()
                }
            }
        }
    }

    override fun register(context: DotnetBuildContext) {
        if (
                buildCommands.contains(context.command.commandType)
                && context.toolVersion > Version.LastVersionWithoutSharedCompilation
                && _parametersService.tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)?.equals("true", true) ?: true) {
            _workingDirectories.getOrPut(context.toolVersion) { context.workingDirectory }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(BuildServerShutdownMonitor::class.java)
        internal val shutdownArgs = listOf(CommandLineArgument("build-server"), CommandLineArgument("shutdown"))
        internal val UseSharedCompilationEnvVarName = "UseSharedCompilation"
        private val buildCommands = setOf(
                DotnetCommandType.Build,
                DotnetCommandType.Pack,
                DotnetCommandType.Publish,
                DotnetCommandType.Test,
                DotnetCommandType.Run,
                DotnetCommandType.MSBuild)
    }
}