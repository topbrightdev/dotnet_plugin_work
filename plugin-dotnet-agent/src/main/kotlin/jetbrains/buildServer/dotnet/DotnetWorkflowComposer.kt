package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import org.apache.log4j.Logger
import java.io.Closeable
import java.util.*

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet,
        private val _failedTestSource: FailedTestSource,
        private val _targetRegistry: TargetRegistry,
        private val _commandRegistry: CommandRegistry,
        private val _contextFactory: DotnetBuildContextFactory,
        private val _versionParser: VersionParser)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            Workflow(sequence {
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                var dotNetVersion = Version.Empty
                for (command in _commandSet.commands) {
                    if (command.toolResolver.paltform == ToolPlatform.DotnetCore && dotNetVersion == Version.Empty) {
                        val dotnetExecutableFile = command.toolResolver.executableFile
                        val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
                        context.subscribe {
                            when {
                                it is CommandResultOutput -> {
                                    _versionParser.tryParse(sequenceOf(it.output))?.let {
                                        dotNetVersion = Version.parse(it)
                                    }
                                }
                                it is CommandResultExitCode -> { }
                            }
                        }.use {
                            yield(CommandLine(TargetType.SystemDiagnostics, dotnetExecutableFile, workingDirectory, versionArgs, emptyList()))
                        }
                    }

                    LOG.debug("Create the build context.")
                    val dotnetBuildContext = _contextFactory.create(command)
                    val result = EnumSet.noneOf(CommandResult::class.java)

                    LOG.debug("Build the environment.")
                    val environmentTokens = mutableListOf<Closeable>()
                    for (environmentBuilder in command.environmentBuilders) {
                        environmentTokens.add(environmentBuilder.build(dotnetBuildContext))
                    }

                    try {
                        val executableFile = command.toolResolver.executableFile
                        val args = command.getArguments(dotnetBuildContext).toList()
                        val commandHeader = _argumentsService.combine(sequenceOf(executableFile.name).plus(args.map { it.value }))
                        _loggerService.writeStandardOutput(
                                Pair(".NET Core SDK v${dotnetBuildContext.currentSdk.version} ", Color.Default),
                                Pair(commandHeader, Color.Header))
                        val commandType = command.commandType
                        val commandName = commandType.id.replace('-', ' ')
                        val blockName = if (commandName.isNotBlank()) {
                            commandName
                        } else {
                            args.firstOrNull()?.value ?: ""
                        }

                        _loggerService.writeBlock(blockName).use {
                            _failedTestSource
                                    .subscribe { result += CommandResult.FailedTests }
                                    .use {
                                        _targetRegistry.activate(target).use {
                                            _commandRegistry.register(dotnetBuildContext)
                                            yield(CommandLine(
                                                    TargetType.Tool,
                                                    executableFile,
                                                    dotnetBuildContext.workingDirectory,
                                                    args,
                                                    _defaultEnvironmentVariables.getVariables(dotnetBuildContext.currentSdk.version).toList()))
                                        }
                                    }
                        }
                    } finally {
                        LOG.debug("Clean the environment.")
                        for (environmentToken in environmentTokens) {
                            try {
                                environmentToken.close()
                            } catch (ex: Exception) {
                                LOG.error("Error during cleaning the environment.", ex)
                            }
                        }
                    }

                    val exitCode = context.lastResult.exitCode
                    val commandResult = command.resultsAnalyzer.analyze(exitCode, result)
                    _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, exitCode)
                    if (commandResult.contains(CommandResult.Fail)) {
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                    }
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
            })

    companion object {
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        private val LOG = Logger.getLogger(DotnetWorkflowComposer::class.java)
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}