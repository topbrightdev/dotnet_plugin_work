package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetDeleteArgumentsProviderTest {
    @DataProvider
    fun testNugetDeleteArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_NUGET_DELETE_ID to "id version",
                        DotnetConstants.PARAM_NUGET_DELETE_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_DELETE_SOURCE to "http://jb.com"),
                        listOf("id", "version", "--api-key", "key",
                                "--source", "http://jb.com", "--non-interactive"))
        )
    }

    @Test(dataProvider = "testNugetDeleteArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = NugetDeleteArgumentsProvider(ParametersServiceStub(parameters))

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommand() {
        // Given
        val argumentsProvider = NugetDeleteArgumentsProvider(ParametersServiceStub(emptyMap()))

        // When
        val actualCommand = argumentsProvider.command

        // Then
        Assert.assertEquals(actualCommand, DotnetCommand.NuGetDelete)
    }
}
