package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.Closeable
import java.io.File

class DotnetWorkflowComposerTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _workflowContext: WorkflowContext? = null
    private var _environmentBuilder1: EnvironmentBuilder? = null
    private var _environmentVariables: EnvironmentVariables? = null
    private var _commandSet: CommandSet? = null
    private var _dotnetCommand1: DotnetCommand? = null
    private var _dotnetCommand2: DotnetCommand? = null
    private var _toolResolver1: ToolResolver? = null
    private var _toolResolver2: ToolResolver? = null
    private var _closeable1: Closeable? = null
    private var _loggerService: LoggerService? = null
    private var _closeable3: Closeable? = null
    private var _closeable4: Closeable? = null
    private var _failedTestDetector: FailedTestDetector = FailedTestDetectorStub(false)

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _loggerService = _ctx!!.mock(LoggerService::class.java)
        _workflowContext = _ctx!!.mock(WorkflowContext::class.java)
        _environmentVariables = _ctx!!.mock(EnvironmentVariables::class.java)
        _commandSet = _ctx!!.mock(CommandSet::class.java)
        _dotnetCommand1 = _ctx!!.mock(DotnetCommand::class.java, "command1")
        _environmentBuilder1 = _ctx!!.mock(EnvironmentBuilder::class.java)
        _toolResolver1 = _ctx!!.mock(ToolResolver::class.java, "resolver1")
        _closeable1 = _ctx!!.mock(Closeable::class.java, "closeable1")
        _dotnetCommand2 = _ctx!!.mock(DotnetCommand::class.java, "command2")
        _toolResolver2 = _ctx!!.mock(ToolResolver::class.java, "resolver2")
        _closeable3 = _ctx!!.mock(Closeable::class.java, "closeable3")
        _closeable4 = _ctx!!.mock(Closeable::class.java, "closeable4")
    }

    @Test
    fun shouldCompose() {
        // Given
        val workingDirectory = File("workingDir")
        val composer = createInstance()
        val envVars = listOf<CommandLineEnvironmentVariable>(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var1", "val1"))
        val args1 = listOf<CommandLineArgument>(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
        val args2 = listOf<CommandLineArgument>(CommandLineArgument("arg3"))
        val result = CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                allowing<EnvironmentVariables>(_environmentVariables).variables
                will(returnValue(envVars.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).commandType
                will(returnValue(DotnetCommandType.Build))

                oneOf<DotnetCommand>(_dotnetCommand1).arguments
                will(returnValue(args1.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).environmentBuilders
                will(returnValue(sequenceOf(_environmentBuilder1)))

                oneOf<DotnetCommand>(_dotnetCommand1).isSuccessful(result)
                will(returnValue(true))

                oneOf<DotnetCommand>(_dotnetCommand1).toolResolver
                will(returnValue(_toolResolver1))

                oneOf<ToolResolver>(_toolResolver1).executableFile
                will(returnValue(File("dotnet.exe")))

                oneOf<DotnetCommand>(_dotnetCommand2).commandType
                will(returnValue(DotnetCommandType.NuGetPush))

                oneOf<DotnetCommand>(_dotnetCommand2).arguments
                will(returnValue(args2.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand2).environmentBuilders
                will(returnValue(emptySequence<EnvironmentBuilder>()))

                oneOf<DotnetCommand>(_dotnetCommand2).isSuccessful(result)
                will(returnValue(true))

                oneOf<DotnetCommand>(_dotnetCommand2).toolResolver
                will(returnValue(_toolResolver2))

                oneOf<ToolResolver>(_toolResolver2).executableFile
                will(returnValue(File("msbuild.exe")))

                oneOf<CommandSet>(_commandSet).commands
                will(returnValue(sequenceOf(_dotnetCommand1, _dotnetCommand2)))

                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(result))

                oneOf<LoggerService>(_loggerService).onStandardOutput("dotnet.exe arg1 arg2")

                oneOf<LoggerService>(_loggerService).onBlock(DotnetCommandType.Build.id)
                will(returnValue(_closeable3))

                oneOf<Closeable>(_closeable3).close()

                oneOf<LoggerService>(_loggerService).onStandardOutput("msbuild.exe arg3")

                oneOf<LoggerService>(_loggerService).onBlock(DotnetCommandType.NuGetPush.id.replace('-', ' '))
                will(returnValue(_closeable4))

                oneOf<Closeable>(_closeable4).close()

                oneOf<EnvironmentBuilder>(_environmentBuilder1).build(_dotnetCommand1!!)
                will(returnValue(_closeable1))

                oneOf<Closeable>(_closeable1).close()

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(result))

                oneOf<WorkflowContext>(_workflowContext).registerOutputFilter(composer)
            }
        })

        val actualCommandLines = composer.compose(_workflowContext!!).commandLines.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                File("dotnet.exe"),
                                workingDirectory,
                                args1,
                                envVars),
                        CommandLine(
                                TargetType.Tool,
                                File("msbuild.exe"),
                                workingDirectory,
                                args2,
                                envVars)
                ))
    }

    private fun createInstance(): DotnetWorkflowComposer {
        return DotnetWorkflowComposer(
                _pathService!!,
                _loggerService!!,
                _failedTestDetector,
                ArgumentsServiceStub(),
                _environmentVariables!!,
                _commandSet!!)
    }
}