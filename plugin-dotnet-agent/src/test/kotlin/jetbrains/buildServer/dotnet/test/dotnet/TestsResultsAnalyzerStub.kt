package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import java.util.*

class TestsResultsAnalyzerStub(): ResultsAnalyzer {
    override fun analyze(result: CommandLineResult): EnumSet<CommandResult> =
            EnumSet.of(CommandResult.Success)
}