package jetbrains.buildServer.agent

import jetbrains.buildServer.util.StringUtil

class ArgumentsServiceImpl : ArgumentsService {

    override fun split(text: String) =
            StringUtil.splitCommandArgumentsAndUnquote(text)
                    .asSequence()
                    .filter { !it.isNullOrBlank() }

    override fun combine(arguments: Sequence<String>, argumentsSeparator: String): String =
            arguments
                    .map {
                        if (it.contains(ArgumentsService.ArgsSeparator)) {
                            "${QuoteString}${it}${QuoteString}"
                        }
                        else {
                            it
                        }
                    }
                    .joinToString(argumentsSeparator);

    override fun escape(text: String): String {
        return StringUtil.escapeStringCharacters(text)
    }

    companion object {
        private val QuoteString = "\""
    }
}