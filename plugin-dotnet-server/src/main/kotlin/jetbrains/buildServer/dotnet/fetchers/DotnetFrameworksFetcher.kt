/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides frameworks fetcher for project model.
 */
class DotnetFrameworksFetcher(private val _solutionDiscover: SolutionDiscover) : ProjectDataFetcher {
    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
        getValues(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                .map { DataItem(it, null) }
                .toMutableList()

    fun getValues(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<String> =
        _solutionDiscover.discover(streamFactory, paths)
                .flatMap { it.projects.asSequence() }
                .flatMap { it.frameworks.asSequence() }
                .map { it.name }
                .distinctBy() { it.toLowerCase() }
                .sorted()

    override fun getType(): String {
        return "DotnetFrameworks"
    }
}
