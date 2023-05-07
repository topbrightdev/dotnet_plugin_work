package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommand

interface DotnetCommandArgumentsProvider: ArgumentsProvider {
    val targetArguments: Sequence<TargetArguments>
    val command: DotnetCommand
}