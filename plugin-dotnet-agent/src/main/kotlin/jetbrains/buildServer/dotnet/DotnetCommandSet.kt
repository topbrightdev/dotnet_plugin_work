package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetCommandSet(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _MSBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _verbosityArgumentsProvider: ArgumentsProvider,
        commands: List<DotnetCommand>)
    : CommandSet {

    private val _knownCommands: Map<String, DotnetCommand> = commands.associateBy({ it.commandType.id }, { it })

    override val commands: Sequence<DotnetCommand>
        get() {
            val commandName = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
            if (commandName.isNullOrBlank()) {
                throw RunBuildException("Dotnet id name is empty")
            }

            val command = _knownCommands[commandName] ?: throw RunBuildException("Unknown dotnet command type \"$commandName\"")
            return getTargetArguments(command).map { CompositeCommand(command, getArguments(command, it)) }
        }

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
            targetArguments: TargetArguments): Sequence<CommandLineArgument> {
        return buildSequence {
            // id
            yieldAll(command.commandType.args.map { CommandLineArgument(it) })
            // projects
            yieldAll(targetArguments.arguments)
            // id specific arguments
            yieldAll(command.arguments)
            // verbosity level
            yieldAll(_verbosityArgumentsProvider.arguments)
            // custom arguments
            yieldAll(_customArgumentsProvider.arguments)
            // logger
            yieldAll(_MSBuildLoggerArgumentsProvider.arguments)
        };
    }

    class CompositeCommand(
            private val _command: DotnetCommand,
            private val _arguments: Sequence<CommandLineArgument>)
        : DotnetCommand {

        override val commandType: DotnetCommandType
            get() = _command.commandType

        override val arguments: Sequence<CommandLineArgument>
            get() = _arguments

        override val targetArguments: Sequence<TargetArguments>
            get() = emptySequence<TargetArguments>()

        override fun isSuccess(exitCode: Int) = _command.isSuccess(exitCode)
    }
}