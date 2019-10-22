package jetbrains.buildServer.sh

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class ShWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext,
        private val _pathResolverWorkflowFactory: PathResolverWorkflowFactory)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, workflow: Workflow) =
            when (_virtualContext.targetOSType) {
                OSType.UNIX, OSType.MAC -> {
                    Workflow(sequence {
                        var shExecutable: Path? = null
                        for (originalCommandLine in workflow.commandLines) {
                            when (originalCommandLine.executableFile.extension().toLowerCase()) {
                                "sh" -> {
                                    if (shExecutable == null ) {
                                        var state = PathResolverState(Path("sh"))
                                        yieldAll(_pathResolverWorkflowFactory.create(context, state).commandLines)
                                        shExecutable = state.resolvedPath
                                    }

                                    yield(CommandLine(
                                            TargetType.Host,
                                            shExecutable ?: Path( "sh"),
                                            originalCommandLine.workingDirectory,
                                            getArguments(originalCommandLine).toList(),
                                            originalCommandLine.environmentVariables,
                                            originalCommandLine.title,
                                            originalCommandLine.description))
                                }
                                else -> yield(originalCommandLine)
                            }
                        }
                    })
                }
                else -> workflow
            }

    private fun getArguments(commandLine: CommandLine) = sequence {
        yield(CommandLineArgument("-c"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}