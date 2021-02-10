package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_SDK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType

class RequirementFactoryImpl(
        private val _sdkTypeResolver: SdkTypeResolver)
    : RequirementFactory {
    override fun tryCreate(sdkVersion: String) =
        Version.tryParse(sdkVersion)?.let {
            version ->
            _sdkTypeResolver.tryResolve(version)?.let {
                when(it) {
                    SdkType.Dotnet, SdkType.DotnetCore -> Requirement("$EXISTS_QUALIFIER$CONFIG_PREFIX_CORE_SDK$version[\\.\\d]*$CONFIG_SUFFIX_PATH", null, RequirementType.EXISTS)
                    SdkType.FullDotnet -> Requirement("$EXISTS_QUALIFIER$CONFIG_PREFIX_DOTNET_FAMEWORK$version[\\.\\d]*_x[\\d]{2}", null, RequirementType.EXISTS)
                }
            }
        }
}