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

package jetbrains.buildServer.script.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.discovery.SolutionDiscoverImpl
import jetbrains.buildServer.dotnet.discovery.StreamFactory
import kotlinx.coroutines.launch
import java.io.File

class ScriptDiscoverImpl: ScriptDiscover {
    override fun discover(streamFactory: StreamFactory, paths: Sequence<String>) = sequence {
        for (path in paths) {
            LOG.debug("Discover \"$path\"")
            try {
                if("csx".equals(File(path).extension, true)) {
                    yield(Script(path))
                }
            } catch (ex: Exception) {
                LOG.debug("Discover error for \"$path\": ${ex.message}")
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(ScriptDiscoverImpl::class.java.name)
    }
}