package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.ArgumentsService

class ArgumentsServiceStub : ArgumentsService {
    override fun escape(text: String): String = text

    override fun split(text: String): Sequence<String> {
        return jetbrains.buildServer.util.StringUtil.splitCommandArgumentsAndUnquote(text)
                .asSequence()
                .filter { !it.isNullOrBlank() }
    }

    override fun combine(arguments: Sequence<String>, argumentsSeparator: String): String = arguments.joinToString(argumentsSeparator)
}