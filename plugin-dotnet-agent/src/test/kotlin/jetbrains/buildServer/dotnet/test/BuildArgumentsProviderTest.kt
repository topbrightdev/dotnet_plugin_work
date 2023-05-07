package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.BuildArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class BuildArgumentsProviderTest {
    @DataProvider
    fun testBuildArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("build", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_BUILD_CONFIG, "Release")),
                        listOf("build", "--framework", "dnxcore50", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_OUTPUT, "output/")),
                        listOf("build", "--output", "output/")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_BUILD_NON_INCREMENTAL to " true",
                        DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES to "True "),
                        listOf("build", "--no-incremental", "--no-dependencies")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_BUILD_VERSION_SUFFIX to " preview ",
                        DotnetConstants.PARAM_VERBOSITY to "normal"),
                        listOf("build", "--version-suffix", "preview", "--verbosity", "normal")))
    }

    @Test(dataProvider = "testBuildArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = BuildArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}