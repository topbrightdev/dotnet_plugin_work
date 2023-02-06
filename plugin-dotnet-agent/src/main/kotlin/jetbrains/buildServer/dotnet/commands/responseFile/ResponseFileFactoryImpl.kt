/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.commands.responseFile

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterConverter
import jetbrains.buildServer.rx.use
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class ResponseFileFactoryImpl(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _loggerService: LoggerService,
    private val _msBuildParameterConverter: MSBuildParameterConverter,
    private val _virtualContext: VirtualContext)
    : ResponseFileFactory {
    override fun createResponeFile(
        description: String,
        arguments: Sequence<CommandLineArgument>,
        parameters: Sequence<MSBuildParameter>,
        verbosity: Verbosity?): Path {
        val args = (
                arguments
                + _msBuildParameterConverter.convert(parameters).map { CommandLineArgument(it) })
                .toList()

        verbosity?.let {
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock("$BlockName $description".trim()).use {
                        for ((value) in args) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }
                    }
                }
                else -> { }
            }
        }

        val msBuildResponseFile = _pathsService.getTempFileName("$description$ResponseFileExtension")
        _fileSystemService.write(msBuildResponseFile) {
            // BOM
            it.write(BOM)
            OutputStreamWriter(it, StandardCharsets.UTF_8).use {
                for (arg in args) {
                    it.write(arg.value)
                    it.write("\n")
                }
            }
        }

        return Path(_virtualContext.resolvePath(msBuildResponseFile.path))
    }

    companion object {
        private val LOG = Logger.getLogger(ResponseFileFactoryImpl::class.java)
        internal val BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "Response File"
    }
}