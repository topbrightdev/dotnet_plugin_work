package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_VISUAL_STUDIO
import jetbrains.buildServer.agent.Logger

class VisualStudioAgentPropertiesProvider(
        private val _visualStudioProviders: List<ToolInstanceProvider>)
    : AgentPropertiesProvider {

    override val desription = "Visual Studio"

    override val properties =
            _visualStudioProviders
                    .asSequence()
                    .flatMap { it.getInstances() }
                    .filter { it.toolType == ToolInstanceType.VisualStudio }
                    .distinctBy { it.baseVersion }
                    .flatMap {
                        visualStudio ->
                        LOG.debug("Found ${visualStudio}.")
                        sequence {
                            yield(AgentProperty(ToolInstanceType.VisualStudio, "$CONFIG_PREFIX_VISUAL_STUDIO${visualStudio.baseVersion}", "${visualStudio.detailedVersion}"))
                            yield(AgentProperty(ToolInstanceType.VisualStudio, "$CONFIG_PREFIX_VISUAL_STUDIO${visualStudio.baseVersion}_Path", visualStudio.installationPath.path))
                        }
                    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioAgentPropertiesProvider::class.java)
    }
}