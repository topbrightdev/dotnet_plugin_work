/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet restore command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RestoreArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProvider {

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument(DotnetConstants.COMMAND_RESTORE))

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yieldAll(_argumentsService.split(it).map { CommandLineArgument(it) })
        }

        parameters(DotnetConstants.PARAM_RESTORE_PACKAGES)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--packages"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_RESTORE_SOURCE)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_PARALLEL, "").trim().toBoolean()) {
            yield(CommandLineArgument("--disable-parallel"))
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_NO_CACHE, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-cache"))
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_IGNORE_FAILED, "").trim().toBoolean()) {
            yield(CommandLineArgument("--ignore-failed-sources"))
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_ROOT_PROJECT, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-dependencies"))
        }

        parameters(DotnetConstants.PARAM_RESTORE_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configfile"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--verbosity"))
                yield(CommandLineArgument(it))
            }
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}