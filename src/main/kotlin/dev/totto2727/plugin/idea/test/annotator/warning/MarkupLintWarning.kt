package dev.totto2727.plugin.idea.test.annotator.warning

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream


@Serializable
data class MarkupLintWarning(
    val line: Int,
    val column: Int,
    val reason: String
) {
    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        private val decoder = Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
        }

        fun fromJson(stream: InputStream) = decoder.decodeFromStream<List<MarkupLintWarning>>(stream)
    }
}
