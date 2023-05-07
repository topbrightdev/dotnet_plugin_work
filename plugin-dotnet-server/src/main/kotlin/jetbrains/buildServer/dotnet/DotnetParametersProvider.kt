/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.*

/**
 * Provides parameters for dotnet runner.
 */
class DotnetParametersProvider {

    val types: Collection<CommandType> = commandTypes.values

    val commandKey: String
        get() = DotnetConstants.PARAM_COMMAND

    val pathsKey: String
        get() = DotnetConstants.PARAM_PATHS

    val argumentsKey: String
        get() = DotnetConstants.PARAM_ARGUMENTS

    val verbosityKey: String
        get() = DotnetConstants.PARAM_VERBOSITY

    val restorePackagesKey: String
        get() = DotnetConstants.PARAM_RESTORE_PACKAGES

    val restoreParallelKey: String
        get() = DotnetConstants.PARAM_RESTORE_PARALLEL

    val restoreConfigKey: String
        get() = DotnetConstants.PARAM_RESTORE_CONFIG

    val restoreSourceKey: String
        get() = DotnetConstants.PARAM_RESTORE_SOURCE

    val restoreNoCacheKey: String
        get() = DotnetConstants.PARAM_RESTORE_NO_CACHE

    val restoreIgnoreFailedKey: String
        get() = DotnetConstants.PARAM_RESTORE_IGNORE_FAILED

    val restoreRootProjectKey: String
        get() = DotnetConstants.PARAM_RESTORE_ROOT_PROJECT

    val buildFrameworkKey: String
        get() = DotnetConstants.PARAM_BUILD_FRAMEWORK

    val buildConfigKey: String
        get() = DotnetConstants.PARAM_BUILD_CONFIG

    val buildRuntimeKey: String
        get() = DotnetConstants.PARAM_BUILD_RUNTIME

    val buildNonIncrementalKey: String
        get() = DotnetConstants.PARAM_BUILD_NON_INCREMENTAL

    val buildNoDependenciesKey: String
        get() = DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES

    val buildOutputKey: String
        get() = DotnetConstants.PARAM_BUILD_OUTPUT

    val buildVersionSuffixKey: String
        get() = DotnetConstants.PARAM_BUILD_VERSION_SUFFIX

    val publishFrameworkKey: String
        get() = DotnetConstants.PARAM_PUBLISH_FRAMEWORK

    val publishConfigKey: String
        get() = DotnetConstants.PARAM_PUBLISH_CONFIG

    val publishOutputKey: String
        get() = DotnetConstants.PARAM_PUBLISH_OUTPUT

    val publishRuntimeKey: String
        get() = DotnetConstants.PARAM_PUBLISH_RUNTIME

    val publishVersionSuffixKey: String
        get() = DotnetConstants.PARAM_PUBLISH_VERSION_SUFFIX

    val packConfigKey: String
        get() = DotnetConstants.PARAM_PACK_CONFIG

    val packOutputKey: String
        get() = DotnetConstants.PARAM_PACK_OUTPUT

    val packTempKey: String
        get() = DotnetConstants.PARAM_PACK_TEMP

    val packVersionSuffixKey: String
        get() = DotnetConstants.PARAM_PACK_VERSION_SUFFIX

    val packNoBuildKey: String
        get() = DotnetConstants.PARAM_PACK_NO_BUILD

    val packServiceableKey: String
        get() = DotnetConstants.PARAM_PACK_SERVICEABLE

    val testFrameworkKey: String
        get() = DotnetConstants.PARAM_TEST_FRAMEWORK

    val testConfigKey: String
        get() = DotnetConstants.PARAM_TEST_CONFIG

    val testOutputKey: String
        get() = DotnetConstants.PARAM_TEST_OUTPUT

    val testTempKey: String
        get() = DotnetConstants.PARAM_TEST_TEMP

    val testRuntimeKey: String
        get() = DotnetConstants.PARAM_TEST_RUNTIME

    val testNoBuildKey: String
        get() = DotnetConstants.PARAM_TEST_NO_BUILD

    val runFrameworkKey: String
        get() = DotnetConstants.PARAM_RUN_FRAMEWORK

    val runConfigKey: String
        get() = DotnetConstants.PARAM_RUN_CONFIG

    val nugetPushApiKey: String
        get() = DotnetConstants.PARAM_NUGET_PUSH_API_KEY

    val nugetPushSourceKey: String
        get() = DotnetConstants.PARAM_NUGET_PUSH_SOURCE

    val nugetPushNoBufferKey: String
        get() = DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER

    val nugetPushNoSymbolsKey: String
        get() = DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS

    val nugetDeleteIdKey: String
        get() = DotnetConstants.PARAM_NUGET_DELETE_ID

    val nugetDeleteApiKey: String
        get() = DotnetConstants.PARAM_NUGET_DELETE_API_KEY

    val nugetDeleteSourceKey: String
        get() = DotnetConstants.PARAM_NUGET_DELETE_SOURCE

    val cleanFrameworkKey: String
        get() = DotnetConstants.PARAM_CLEAN_FRAMEWORK

    val cleanConfigKey: String
        get() = DotnetConstants.PARAM_CLEAN_CONFIG

    val cleanRuntimeKey: String
        get() = DotnetConstants.PARAM_CLEAN_RUNTIME

    val cleanOutputKey: String
        get() = DotnetConstants.PARAM_CLEAN_OUTPUT

    val verbosity: List<String>
        get() = listOf("Quiet", "Minimal", "Normal", "Detailed", "Diagnostic")

    val integrationPackagePathKey: String
        get() = DotnetConstants.INTEGRATION_PACKAGE_HOME

    val integrationPackageToolTypeKey: String
        get() = DotnetConstants.PACKAGE_TYPE

    val dotCoverToolTypeKey: String
        get() = DotCoverConstants.PARAM_TOOL_TYPE_ID

    val dotCoverEnabledKey: String
        get() = DotCoverConstants.PARAM_ENABLED

    val dotCoverHomeKey: String
        get() = DotCoverConstants.PARAM_HOME

    val dotCoverFiltersKey: String
        get() = DotCoverConstants.PARAM_FILTERS

    val dotCoverAttributeFiltersKey: String
        get() = DotCoverConstants.PARAM_ATTRIBUTE_FILTERS

    val dotCoverArgumentsKey: String
        get() = DotCoverConstants.PARAM_ARGUMENTS

    val msbuildVersionKey: String
        get() = DotnetConstants.PARAM_MSBUILD_VERSION

    val msbuildVersions: List<Tool>
        get() = Tool.values().filter { it.type == ToolType.MSBuild }

    val msbuildTargetsKey: String
        get() = DotnetConstants.PARAM_MSBUILD_TARGETS

    val msbuildConfigKey: String
        get() = DotnetConstants.PARAM_MSBUILD_CONFIG

    val msbuildPlatformKey: String
        get() = DotnetConstants.PARAM_MSBUILD_PLATFORM

    val vstestVersionKey: String
        get() = DotnetConstants.PARAM_VSTEST_VERSION

    val vstestVersions: List<Tool>
        get() = Tool.values().filter { it.type == ToolType.VSTest }

    val vstestConfigFileKey: String
        get() = DotnetConstants.PARAM_VSTEST_CONFIG_FILE

    val vstestPlatformKey: String
        get() = DotnetConstants.PARAM_VSTEST_PLATFORM

    val vstestFrameworkKey: String
        get() = DotnetConstants.PARAM_VSTEST_FRAMEWORK

    val vstestTestNamesKey: String
        get() = DotnetConstants.PARAM_VSTEST_TEST_NAMES

    val vstestTestCaseFilterKey: String
        get() = DotnetConstants.PARAM_VSTEST_TEST_CASE_FILTER

    val vstestInIsolationKey: String
        get() = DotnetConstants.PARAM_VSTEST_IN_ISOLATION

    companion object {
        val dotCoverInfoProvider: DotCoverInfoProvider = DotCoverInfoProvider()
        val commandTypes = listOf(
                RestoreCommandType(),
                BuildCommandType(),
                TestCommandType(dotCoverInfoProvider),
                PublishCommandType(),
                PackCommandType(),
                NugetPushCommandType(),
                NugetDeleteCommandType(),
                CleanCommandType(),
                RunCommandType(),
                MSBuildCommandType(MSBuildRequirementsProvider(dotCoverInfoProvider), dotCoverInfoProvider),
                VSTestCommandType(VSTestRequirementsProvider(dotCoverInfoProvider), dotCoverInfoProvider)
        ).associateBy { it.name }
    }
}
