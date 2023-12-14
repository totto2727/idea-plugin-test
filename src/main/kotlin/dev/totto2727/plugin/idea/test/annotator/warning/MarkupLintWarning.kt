package dev.totto2727.plugin.idea.test.annotator.warning

import kotlinx.serialization.Serializable

@Serializable
data class MarkupLintWarning(
    val line: Int,
    val column: Int,
    val reason: String
) {
}
