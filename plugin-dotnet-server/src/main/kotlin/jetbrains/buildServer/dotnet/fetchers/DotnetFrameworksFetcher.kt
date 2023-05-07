/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project

/**
 * Provides frameworks fetcher for project model.
 */
class DotnetFrameworksFetcher(modelParser: DotnetModelParser) : DotnetProjectsDataFetcher(modelParser) {

    override fun getDataItems(project: Project?): Collection<String> {
        return project?.frameworks?.keys ?: emptySet()
    }

    override fun getDataItems(project: CsProject?): Collection<String> {
        project?.let {
            it.propertyGroups?.let {
                return it.fold(hashSetOf(), {
                    all, current ->
                    current.targetFramework?.let {
                        all.add(it)
                    }
                    current.targetFrameworks?.let {
                        all.addAll(it.split(';'))
                    }
                    all
                })
            }
        }

        return emptyList()
    }

    override fun getType(): String {
        return "DotnetFrameworks"
    }
}
