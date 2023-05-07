package jetbrains.buildServer.agent

import jetbrains.buildServer.dotnet.JsonVisualStudioInstanceParser
import org.apache.log4j.Logger
import java.io.Reader

class JsonParserImpl : JsonParser {
    override fun <T> tryParse(reader: Reader, classOfT: Class<T>): T? =
        Gson.fromJson<T>(reader, classOfT)

    companion object {
        private val Gson = com.google.gson.Gson()
    }
}