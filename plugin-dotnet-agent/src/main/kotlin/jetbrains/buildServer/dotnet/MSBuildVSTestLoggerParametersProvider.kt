package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class MSBuildVSTestLoggerParametersProvider(
        private val _pathsService: PathsService,
        private val _loggerResolver: LoggerResolver,
        private val _testReportingParameters: TestReportingParameters)
    : MSBuildParametersProvider {

    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            var testReportingMode = _testReportingParameters.Mode;
            if (testReportingMode.contains(TestReportingMode.Off)) {
                return@buildSequence
            }

            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(MSBuildParameter("VSTestLogger", "logger://teamcity"))
                if(testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
                    yield(MSBuildParameter("VSTestTestAdapterPath", "${it.absolutePath};."))
                }
                else {
                    yield(MSBuildParameter("VSTestTestAdapterPath", _pathsService.getPath(PathType.Checkout).absolutePath))
                }
            }
        }
}