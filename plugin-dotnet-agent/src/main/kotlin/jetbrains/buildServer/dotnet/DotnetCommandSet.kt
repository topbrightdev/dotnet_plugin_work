package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetCommandSet(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        commands: List<DotnetCommand>)
    : CommandSet {

    private val _knownCommands: Map<String, DotnetCommand> = commands.associateBy({ it.commandType.id }, { it })

    override val commands: Sequence<DotnetCommand> get() =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)?.let {
                _knownCommands[it]?.let {
                    val command = it
                    getTargetArguments(command).asSequence().map {
                        val targetArguments = TargetArguments(it.arguments.toList().asSequence())
                        CompositeCommand(command, getArguments(command, targetArguments), targetArguments)
                    }
                }
            }  ?: emptySequence()

    private fun getTargetArguments(command: DotnetCommand): Sequence<TargetArguments>
    {
        return buildSequence {
            var hasTargets = false;
            for(targetArguments in command.targetArguments)
            {
                yield(targetArguments)
                hasTargets = true
            }

            if (!hasTargets) {
                yield(TargetArguments(emptySequence()))
            }
        }
    }

    private fun getArguments(
        command: DotnetCommand,
        targetArguments: TargetArguments): Sequence<CommandLineArgument> =
        buildSequence {
            if(command.toolResolver.isCommandRequired) {
                // command
                yieldAll(command.commandType.args.map { CommandLineArgument(it) })
            }

            // projects
            yieldAll(targetArguments.arguments)
            // command specific arguments
            yieldAll(command.arguments)
        }

    class CompositeCommand(
            private val _command: DotnetCommand,
            private val _arguments: Sequence<CommandLineArgument>,
            private val _targetArguments: TargetArguments)
        : DotnetCommand {

        override val commandType: DotnetCommandType
            get() = _command.commandType

        override val toolResolver: ToolResolver
            get() = _command.toolResolver

        override val arguments: Sequence<CommandLineArgument>
            get() = _arguments

        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_targetArguments)

        override fun isSuccessfulExitCode(exitCode: Int) = _command.isSuccessfulExitCode(exitCode)
    }
}