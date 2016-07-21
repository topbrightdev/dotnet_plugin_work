/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.dotnet

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.StringUtil

import java.util.ArrayList

/**
 * Provides arguments to dotnet pack command.
 */
class PackArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_PACK)

        val projectsValue = parameters[DotnetConstants.PARAM_PATHS]
        if (!projectsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue!!))
        }

        val basePathValue = parameters[DotnetConstants.PARAM_PACK_BASE]
        if (!basePathValue.isNullOrBlank()) {
            arguments.add("--basepath")
            arguments.add(basePathValue!!.trim())
        }

        val configValue = parameters[DotnetConstants.PARAM_PACK_CONFIG]
        if (!configValue.isNullOrBlank()) {
            arguments.add("--configuration")
            arguments.add(configValue!!.trim())
        }

        val outputValue = parameters[DotnetConstants.PARAM_PACK_OUTPUT]
        if (!outputValue.isNullOrBlank()) {
            arguments.add("--output")
            arguments.add(outputValue!!.trim())
        }

        val tempValue = parameters[DotnetConstants.PARAM_PACK_TEMP]
        if (!tempValue.isNullOrBlank()) {
            arguments.add("--temp-output")
            arguments.add(tempValue!!.trim())
        }

        val versionSuffixValue = parameters[DotnetConstants.PARAM_PACK_VERSION_SUFFIX]
        if (!versionSuffixValue.isNullOrBlank()) {
            arguments.add("--version-suffix")
            arguments.add(versionSuffixValue!!.trim())
        }

        val argumentsValue = parameters[DotnetConstants.PARAM_ARGUMENTS]
        if (!argumentsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue!!))
        }

        return arguments
    }
}
