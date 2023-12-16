package dev.totto2727.plugin.idea.test.annotator.violation

import com.intellij.lang.annotation.HighlightSeverity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

/*
https://github.com/markuplint/markuplint/blob/d7cb34f4a92cebeb4d44b7cebd7916643eee3a6e/packages/%40markuplint/ml-config/src/types.ts#L219

export type Violation = {
    readonly ruleId: string;
    readonly severity: Severity;
    readonly message: string;
    readonly reason?: string;
    readonly line: number;
    readonly col: number;
    readonly raw: string;
};
{ filePath: string }
*/
@Serializable
data class MarkupLintViolation(
    val ruleId: String,
    val severity: Severity,
    val message: String,
    val reason: String? = null,
    val line: Int,
    val col: Int,
    val raw: String,
    val filePath: String
) {
    val lineZeroBase = this.line - 1
    val colZeroBase = this.col - 1

    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        private val decoder = Json

        fun fromJson(stream: InputStream) = decoder.decodeFromStream<List<MarkupLintViolation>>(stream)
    }

    /*
    https://github.com/markuplint/markuplint/blob/d7cb34f4a92cebeb4d44b7cebd7916643eee3a6e/packages/%40markuplint/ml-config/src/types.ts#L175

    export type Severity = 'error' | 'warning' | 'info';
     */
    @Serializable
    enum class Severity {
        @SerialName("error")
        ERROR {
            override fun toHighlightSeverity() = HighlightSeverity.ERROR
        },

        @SerialName("warning")
        WARNING {
            override fun toHighlightSeverity(): HighlightSeverity = HighlightSeverity.WARNING

        },

        @SerialName("info")
        INFO {
            override fun toHighlightSeverity(): HighlightSeverity = HighlightSeverity.INFORMATION
        },

        // add just in case
        UNKNOWN {
            override fun toHighlightSeverity(): HighlightSeverity = HighlightSeverity.WEAK_WARNING
        };

        abstract fun toHighlightSeverity(): HighlightSeverity
    }
}
