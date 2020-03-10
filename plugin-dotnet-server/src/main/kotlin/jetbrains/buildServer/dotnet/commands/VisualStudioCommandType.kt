/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.ToolType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for devenv.com /build command.
 */
class VisualStudioCommandType : CommandType() {

    override val name: String = DotnetCommandType.VisualStudio.id

    override val editPage: String = "editVisualStudioParameters.jsp"

    override val viewPage: String = "viewVisualStudioParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        if (properties[DotnetConstants.PARAM_VISUAL_STUDIO_ACTION].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        if (isDocker(parameters)) return@sequence

        var hasRequirements = false
        parameters[DotnetConstants.PARAM_VISUAL_STUDIO_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.VisualStudio) {
                    yield(Requirement("VS${it.vsVersion}_Path", null, RequirementType.EXISTS))
                    hasRequirements = true
                }
            }
        }

        if (!hasRequirements) {
            yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS))
        }

        yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
    }
}