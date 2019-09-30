package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.StringUtil
import java.io.File

class DotCoverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _coverageFilterProvider: CoverageFilterProvider,
        private val _targetRegistry: TargetRegistry,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        if (!dotCoverEnabled) {
            return workflow
        }

        val dotCoverPath: String?
        val dotCoverExecutableFile: File
        try {
            dotCoverPath = _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            if (dotCoverPath.isNullOrBlank()) {
                return workflow
            }

            dotCoverExecutableFile = File(dotCoverPath, DotCoverExecutableFile).absoluteFile
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        if (!_fileSystemService.isExists(dotCoverExecutableFile)) {
            throw RunBuildException("dotCover was not found: $dotCoverExecutableFile")
        }

        var showDiagnostics = false
        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
            Verbosity.tryParse(it)?.let {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (it) {
                    Verbosity.Detailed, Verbosity.Diagnostic -> {
                        showDiagnostics = true
                    }
                }
            }
        }

        return Workflow(sequence {
            var deferredServiceMessages = mutableListOf<ServiceMessage>()

            _targetRegistry.register(target).use {
                for (commandLineToGetCoverage in workflow.commandLines) {
                    sendServiceMessages(context, deferredServiceMessages)
                    deferredServiceMessages.clear()

                    if (!_targetRegistry.activeTargets.contains(TargetType.Tool)) {
                        yield(commandLineToGetCoverage)
                        continue
                    }

                    val dotCoverProject = DotCoverProject(
                            commandLineToGetCoverage,
                            _pathsService.getTempFileName(DotCoverProjectExtension),
                            _pathsService.getTempFileName(DotCoverSnapshotExtension))

                    _fileSystemService.write(dotCoverProject.configFile) {
                        _dotCoverProjectSerializer.serialize(dotCoverProject, it)
                    }

                    if (showDiagnostics) {
                        _loggerService.writeBlock("dotCover Settings").use {
                            val args = _argumentsService.combine(commandLineToGetCoverage.arguments.map { it.value }.asSequence())
                            _loggerService.writeStandardOutput("Command line:")
                            _loggerService.writeStandardOutput("  \"${commandLineToGetCoverage.executableFile.path}\" $args", Color.Details)

                            _loggerService.writeStandardOutput("Filters:")
                            for (filter in _coverageFilterProvider.filters) {
                                _loggerService.writeStandardOutput("  $filter", Color.Details)
                            }

                            _loggerService.writeStandardOutput("Attribute Filters:")
                            for (filter in _coverageFilterProvider.attributeFilters) {
                                _loggerService.writeStandardOutput("  $filter", Color.Details)
                            }
                        }
                    }

                    yield(CommandLine(
                            target,
                            File(_virtualContext.resolvePath(dotCoverExecutableFile.canonicalPath)),
                            File(_virtualContext.resolvePath(commandLineToGetCoverage.workingDirectory.canonicalPath)),
                            createArguments(dotCoverProject).toList(),
                            commandLineToGetCoverage.environmentVariables))

                    deferredServiceMessages.add(DotCoverServiceMessage(File(dotCoverPath).absoluteFile))
                    deferredServiceMessages.add(ImportDataServiceMessage(DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))
                }
            }

            sendServiceMessages(context, deferredServiceMessages)
        })
    }

    private val dotCoverEnabled
        get(): Boolean {
            _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)?.let {
                if (it == CoverageConstants.PARAM_DOTCOVER) return true
            }

            _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled")?.let {
                if (it.trim().toBoolean()) return true
            }

            return false
        }

    private fun createArguments(dotCoverProject: DotCoverProject) = sequence {
        yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(_virtualContext.resolvePath(dotCoverProject.configFile.canonicalPath), CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument("/ReturnTargetExitCode"))
        yield(CommandLineArgument("/NoCheckForUpdates"))
        yield(CommandLineArgument("/AnalyzeTargetArguments=false"))
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(File(it), "dotCover", ".log").canonicalPath)
            yield(CommandLineArgument("/LogFile=${logFileName}", CommandLineArgumentType.Infrastructural))
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            StringUtil.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }

    private fun sendServiceMessages(context: WorkflowContext, deferredServiceMessages: List<ServiceMessage>) {
        if (context.status == WorkflowStatus.Failed) {
            return
        }

        for (serviceMessage in deferredServiceMessages) {
            _loggerService.writeMessage(serviceMessage)
        }
    }

    companion object {
        internal const val DotCoverExecutableFile = "dotCover.exe"
        internal const val DotCoverToolName = "dotcover"
        internal const val DotCoverProjectExtension = ".dotCover"
        internal const val DotCoverSnapshotExtension = ".dcvr"
    }
}