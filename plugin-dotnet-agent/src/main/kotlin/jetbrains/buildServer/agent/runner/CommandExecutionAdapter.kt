/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyDisposable
import jetbrains.buildServer.rx.emptyObserver
import jetbrains.buildServer.agent.Logger
import java.io.File

class CommandExecutionAdapter(
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService,
        private val _commandLinePresentationService: CommandLinePresentationService,
        private val _virtualContext: VirtualContext,
        private val _programCommandLineFactory: ProgramCommandLineFactory)
    : CommandExecutionFactory, CommandExecution, BuildProgressLoggerAware {
    private var _eventObserver: Observer<CommandResultEvent> = emptyObserver<CommandResultEvent>()
    private var _commandLine: CommandLine = CommandLine(null, TargetType.NotApplicable, Path(""), Path(""))
    private var _blockToken: Disposable = emptyDisposable()

    override fun create(commandLine: CommandLine, eventObserver: Observer<CommandResultEvent>): CommandExecution {
        _commandLine = commandLine
        _eventObserver = eventObserver
        return this
    }

    override fun beforeProcessStarted() = Unit

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        if (!_commandLine.title.isNullOrBlank())
        {
            if (_isHiddenInBuidLog) {
                writeStandardOutput(_commandLine.title)
            }
            else {
                _blockToken = _loggerService.writeBlock(_commandLine.title)
            }
        }

        var commandLine: CommandLine? = _commandLine
        while (commandLine != null) {
            val executableFilePresentation = _commandLinePresentationService.buildExecutablePresentation(commandLine.executableFile)
            val argsPresentation = _commandLinePresentationService.buildArgsPresentation(commandLine.arguments)

            var startingInfo =
                    listOf(StdOutText("Starting: ")) +
                            commandLine.description +
                            executableFilePresentation +
                            argsPresentation

            writeStandardOutput(*(startingInfo).toTypedArray())
            commandLine = commandLine.baseCommandLine
        }

        val virtualWorkingDirectory = _virtualContext.resolvePath(_commandLine.workingDirectory.path)
        writeStandardOutput(StdOutText("in directory: "), StdOutText(virtualWorkingDirectory))
    }

    override fun processFinished(exitCode: Int) {
        _eventObserver.onNext(CommandResultExitCode(exitCode))
        _blockToken.dispose()
    }

    override fun makeProgramCommandLine(): ProgramCommandLine = _programCommandLineFactory.create(_commandLine)

    override fun onStandardOutput(text: String) {
        val event = CommandResultOutput(text)
        _eventObserver.onNext(event)
        if (!event.attributes.contains(CommandResultAttribute.Suppressed)) {
            writeStandardOutput(text)
        }
    }

    override fun onErrorOutput(error: String) {
        val event = CommandResultOutput(error)
        _eventObserver.onNext(CommandResultOutput(error))
        if (!event.attributes.contains(CommandResultAttribute.Suppressed)) {
            _loggerService.writeWarning(error)
        }
    }

    override fun interruptRequested(): TerminationAction = TerminationAction.KILL_PROCESS_TREE

    override fun isCommandLineLoggingEnabled(): Boolean = false

    override fun getLogger(): BuildProgressLogger = SuppressingLogger(_loggerService, _buildStepContext.runnerContext.build.buildLogger, _isHiddenInBuidLog)

    private val _isHiddenInBuidLog get() = _commandLine.chain.any { it.target == TargetType.SystemDiagnostics }

    private fun writeStandardOutput(text: String) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(text)
        }
        else {
            _loggerService.writeTrace(text)
        }
    }

    private fun writeStandardOutput(vararg text: StdOutText) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(*text)
        }
        else {
            _loggerService.writeTrace(text.map { it.text }.joinToString(" "))
        }
    }

    class SuppressingLogger(
            private val _loggerService: LoggerService,
            private val _baseLogger: BuildProgressLogger,
            private val _isHiddenInBuidLog: Boolean):
            BuildProgressLogger by _baseLogger {

        override fun message(message: String?) {
            if (!_isHiddenInBuidLog) {
                _baseLogger.message(message)
            } else {
                if (message != null) {
                    _loggerService.writeTrace(message)
                }
            }
        }
    }
}