package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType

class ProgramCommandLineAdapter(
        private val _argumentsService: ArgumentsService,
        private val _buildStepContext: BuildStepContext)
    : ProgramCommandLine, ProgramCommandLineFactory {

    private var _commandLine: CommandLine = CommandLine(null, TargetType.NotApplicable, Path(""), Path(""))

    override fun getExecutablePath(): String = _commandLine.executableFile.path

    override fun getWorkingDirectory(): String = _commandLine.workingDirectory.path

    override fun getArguments(): MutableList<String> = _commandLine.arguments.map { _argumentsService.normalize(it.value) }.toMutableList()

    override fun getEnvironment(): MutableMap<String, String> {
        val environmentVariables = _buildStepContext.runnerContext.buildParameters.environmentVariables.toMutableMap()
        _commandLine.environmentVariables.forEach { environmentVariables[it.name] = it.value }
        return environmentVariables
    }

    override fun create(commandLine: CommandLine): ProgramCommandLine {
        this._commandLine = commandLine;
        return this;
    }
}