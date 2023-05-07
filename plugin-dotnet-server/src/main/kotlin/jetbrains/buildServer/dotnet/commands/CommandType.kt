/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides command-specific resources.
 */
abstract class CommandType {
    abstract val name: String

    abstract val editPage: String

    abstract val viewPage: String

    open fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> = emptyList()

    abstract fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement>
}
