package jetbrains.buildServer.agent

interface ArgumentsService {
    fun split(text: String): Sequence<String>

    fun combine(arguments: Sequence<String>): String

    fun escape(text: String): String
}