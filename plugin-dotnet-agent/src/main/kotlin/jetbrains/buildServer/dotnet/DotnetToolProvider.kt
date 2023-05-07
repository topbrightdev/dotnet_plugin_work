/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import java.io.File

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
            executblePath
                    ?.absolutePath
                    ?: throw ToolCannotBeFoundException("""
                    Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                    .NET CLI toolchain directory or defined `${DotnetConstants.TOOL_HOME}` variable.""".trimIndent())

    private val executblePath: File? by lazy {
        _toolSearchService.find(DotnetConstants.EXECUTABLE, DotnetConstants.TOOL_HOME)
                .firstOrNull()
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.isVirtualContext) {
            DotnetConstants.EXECUTABLE
        } else {
            getPath(toolName)
        }
    }
}