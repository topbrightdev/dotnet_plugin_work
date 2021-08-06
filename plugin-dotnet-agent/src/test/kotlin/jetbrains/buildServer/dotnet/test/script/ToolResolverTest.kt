package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.script.AnyVersionResolver
import jetbrains.buildServer.script.Framework
import jetbrains.buildServer.script.ScriptConstants
import jetbrains.buildServer.script.ToolResolverImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolResolverTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _anyVersionResolver: AnyVersionResolver
    private val DefaultBasePath = File("basePath")
    private val DotnetPath = "dotnet"

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<out Any?>> {
        return arrayOf(
                // Any version
                arrayOf(
                        DefaultBasePath.path,
                        Framework.Any.tfm,
                        File("myAny"),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultBasePath, "tools"))
                                .addFile(File(File(File("myAny"), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        File(File(File("myAny"), "any"), ToolResolverImpl.ToolExecutable)),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Any.tfm,
                        File("myAny"),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultBasePath, "tools"))
                                .addFile(File(File(File("myAny"), "any"), ToolResolverImpl.ToolExecutable)),
                        File("Abc"),
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Any.tfm,
                        File("myAny"),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultBasePath, "tools"))
                                .addFile(File(File(File("myAny222"), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Any.tfm,
                        File("myAny"),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultBasePath, "tools"))
                                .addDirectory(File(File(File("myAny"), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Any.tfm,
                        File("myAny"),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultBasePath, "tools")),
                        DefaultBasePath,
                        null),

                // Specified version
                arrayOf(
                        DefaultBasePath.path,
                        Framework.Net50.tfm,
                        null,
                        VirtualFileSystemService()
                                .addFile(File(File(File(File(DefaultBasePath, "tools"), Framework.Net50.tfm), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        File(File(File(File(DefaultBasePath, "tools"), Framework.Net50.tfm), "any"), ToolResolverImpl.ToolExecutable)),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Net50.tfm,
                        null,
                        VirtualFileSystemService()
                                .addDirectory(File(File(File(File(DefaultBasePath, "tools"), Framework.Net50.tfm), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Net50.tfm,
                        null,
                        VirtualFileSystemService(),
                        DefaultBasePath,
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        Framework.Net50.tfm,
                        null,
                        VirtualFileSystemService()
                                .addFile(File(File(File(File(DefaultBasePath, "tools"), Framework.Net60.tfm), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null),

                arrayOf(
                        DefaultBasePath.path,
                        null,
                        null,
                        VirtualFileSystemService()
                                .addFile(File(File(File(File(DefaultBasePath, "tools"), Framework.Net50.tfm), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null),

                arrayOf(
                        null,
                        Framework.Net50.tfm,
                        null,
                        VirtualFileSystemService()
                                .addFile(File(File(File(File(DefaultBasePath, "tools"), Framework.Net50.tfm), "any"), ToolResolverImpl.ToolExecutable)),
                        DefaultBasePath,
                        null)
        )
    }

    @Test(dataProvider = "cases")
    fun shouldResolve(
            cltPathParam: String?,
            frameworkParam: String?,
            anyVersionPath: File?,
            fileSystemService: FileSystemService,
            basePath: File,
            expectedPath: File?) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.Runner, ScriptConstants.CLT_PATH) } returns cltPathParam
        every { _parametersService.tryGetParameter(ParameterType.Runner, ScriptConstants.FRAMEWORK) } returns frameworkParam
        if(anyVersionPath != null) {
            every { _anyVersionResolver.resolve(File(basePath, "tools")) } returns anyVersionPath
        }
        else {
            every { _anyVersionResolver.resolve(File(basePath, "tools")) } throws RunBuildException("Some error")
        }

        val resoler = createInstance(fileSystemService)

        // When
        var actualPath: File? = null
        try {
            actualPath = resoler.resolve()
        }
        catch(ex: Exception) { }

        // Then
        Assert.assertEquals(actualPath, expectedPath)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            ToolResolverImpl(_parametersService, fileSystemService, _anyVersionResolver)
}