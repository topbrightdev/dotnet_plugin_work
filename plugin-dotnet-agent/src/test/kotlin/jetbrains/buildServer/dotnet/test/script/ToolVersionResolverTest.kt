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

package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntime
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.script.ToolVersionResolverImpl
import jetbrains.buildServer.script.CsiTool
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolVersionResolverTest {
    @MockK private lateinit var _runtimesProvider: DotnetRuntimesProvider
    @MockK private lateinit var _virtualContext: VirtualContext
    private val DefaultToolsPath = File("tools")
    private val DotnetPath = "dotnet"

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        false,
                        CsiTool(File(DefaultToolsPath, "net5.0"), Version(5, 0))),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(3, 1), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        false,
                        CsiTool(File(DefaultToolsPath, "netcoreapp3.1"), Version(3, 1))),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net6.0")),
                        DefaultToolsPath,
                        false,
                        null),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0, 0, "-beta"), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        false,
                        CsiTool(File(DefaultToolsPath, "net5.0"), Version(5, 0))),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0, 0,"beta"), ""),
                                DotnetRuntime(File("."), Version(3, 1), "")
                        ),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "net6.0"))
                                .addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        false,
                        CsiTool(File(DefaultToolsPath, "net6.0"), Version(6, 0))),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(3, 1), "")
                        ),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        false,
                        CsiTool(File(DefaultToolsPath, "net5.0"), Version(5, 0))),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addFile(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        false,
                        null),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService(),
                        DefaultToolsPath,
                        false,
                        null),

                arrayOf(
                        emptySequence<DotnetRuntime>(),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        false,
                        null),

                arrayOf(
                        emptySequence<DotnetRuntime>(),
                        VirtualFileSystemService(),
                        DefaultToolsPath,
                        false,
                        null),

                // In docker
                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0), "")
                        ),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "net6.0")),
                        DefaultToolsPath,
                        true,
                        CsiTool(File(DefaultToolsPath, "net6.0"), Version(6, 0))),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0), ""),
                                DotnetRuntime(File("."), Version(7, 0), "")
                        ),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "net6.0"))
                                .addDirectory(File(DefaultToolsPath, "net7.0")),
                        DefaultToolsPath,
                        true,
                        CsiTool(File(DefaultToolsPath, "net6.0"), Version(6, 0))),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0), "")
                        ),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        true,
                        CsiTool(File(DefaultToolsPath, "net5.0"), Version(5, 0))),

                arrayOf(
                        emptySequence<DotnetRuntime>(),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net6.0")),
                        DefaultToolsPath,
                        true,
                        CsiTool(File(DefaultToolsPath, "net6.0"), Version(6, 0))),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0), "")
                        ),
                        VirtualFileSystemService(),
                        DefaultToolsPath,
                        true,
                        null),

                arrayOf(
                        emptySequence<DotnetRuntime>(),
                        VirtualFileSystemService(),
                        DefaultToolsPath,
                        true,
                        null)
        )
    }

    @Test(dataProvider = "cases")
    fun shouldResolve(
        runtimes: Sequence<DotnetRuntime>,
        fileSystemService: FileSystemService,
        toolsPath: File,
        isVurtual: Boolean,
        expectedTool: CsiTool?) {
        // Given
        every { _runtimesProvider.getRuntimes() }.returns(runtimes)
        every { _virtualContext.isVirtual } returns isVurtual
        val resoler = createInstance(fileSystemService)

        // When
        var actualTool: CsiTool? = null
        try {
            actualTool = resoler.resolve(toolsPath)
        }
        catch(ex: Exception) { }

        // Then
        Assert.assertEquals(actualTool, expectedTool)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            ToolVersionResolverImpl(fileSystemService, _runtimesProvider, _virtualContext)
}