/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType

/**
 * Provides parameters for dotnet pack command.
 */
class PackCommandType : DotnetType() {
    override val name: String = DotnetCommandType.Pack.id

    override val editPage: String = "editPackParameters.jsp"

    override val viewPage: String = "viewPackParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        yieldAll(super.getRequirements(parameters))

        if (isDocker(parameters)) return@sequence

        if (!parameters[DotnetConstants.PARAM_RUNTIME].isNullOrBlank()) {
            yield(Requirement(DotnetConstants.CONFIG_NAME, "2.0.0", RequirementType.VER_NO_LESS_THAN))
        }
    }
}
