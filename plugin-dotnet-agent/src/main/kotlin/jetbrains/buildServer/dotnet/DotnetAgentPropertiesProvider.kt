/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.AgentPropertyType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import org.apache.log4j.Logger
import java.io.File

/**`
 * Provides a list of available .NET CLI parameters.
 */

class DotnetAgentPropertiesProvider(
        private val _toolProvider: ToolProvider,
        private val _dotnetVersionProvider: DotnetVersionProvider,
        private val _dotnetSdksProvider: DotnetSdksProvider,
        private val _pathsService: PathsService)
    : AgentPropertiesProvider {

    override val desription = ".NET CLI"

    override val properties: Sequence<AgentProperty>
        get() = sequence {
            // Detect .NET CLI path
            val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
            yield(AgentProperty(AgentPropertyType.DotNetCLI, DotnetConstants.CONFIG_PATH, dotnetPath.canonicalPath))

            // Detect .NET CLI version
            val sdkVersion = _dotnetVersionProvider.getVersion(Path(dotnetPath.path), Path(_pathsService.getPath(PathType.Work).path))
            yield(AgentProperty(AgentPropertyType.DotNetCLI, DotnetConstants.CONFIG_NAME, sdkVersion.toString()))

            // Detect .NET SDK
            for ((version, path) in enumerateSdk(_dotnetSdksProvider.getSdks(dotnetPath))) {
                val paramName = "${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}"
                yield(AgentProperty(AgentPropertyType.DotNetSDK, paramName, path))
            }
        }


    companion object {
        private val LOG = Logger.getLogger(DotnetAgentPropertiesProvider::class.java)

        internal fun enumerateSdk(versions: Sequence<DotnetSdk>): Sequence<Pair<String, String>> = sequence {
            val groupedVersions = versions.filter { it.version != Version.Empty }.groupBy { Version(it.version.major, it.version.minor) }
            for ((version, sdks) in groupedVersions) {
                yield("${version.major}.${version.minor}" to sdks.maxBy { it.version }!!.path.absolutePath)
                yieldAll(sdks.map { it.version.toString() to it.path.absolutePath })
            }
        }
    }
}