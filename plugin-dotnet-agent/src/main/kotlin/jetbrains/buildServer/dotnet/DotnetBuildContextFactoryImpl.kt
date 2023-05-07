package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class DotnetBuildContextFactoryImpl(
        private val _pathService: PathsService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _parametersService: ParametersService)
    : DotnetBuildContextFactory {
    override fun create(command: DotnetCommand): DotnetBuildContext =
            DotnetBuildContext(
                    command,
                    getVerbosityLevel(),
                    getSdks().toSet())

    private fun getSdks(): Sequence<DotnetSdk> =
            buildSequence {
                val targetPath = _pathService.getPath(PathType.WorkingDirectory)
                val version = _dotnetCliToolInfo.getVersion(targetPath)
                if (version != Version.Empty) {
                    yield(DotnetSdk(targetPath, version))
                }
            }

    private fun getVerbosityLevel(): Verbosity? =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)
            }
}