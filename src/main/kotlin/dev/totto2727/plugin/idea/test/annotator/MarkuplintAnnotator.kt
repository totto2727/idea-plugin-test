package dev.totto2727.plugin.idea.test.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import dev.totto2727.plugin.idea.test.annotator.warning.MarkupLintWarning


class MarkupLintAnnotator : ExternalAnnotator<Editor, List<MarkupLintWarning>> {
    val logger = Logger.getInstance(javaClass)

    constructor() {
        logger.info("MarkupLintAnnotator")
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasError: Boolean): Editor {
        logger.debug("Called collectInformation");
        return editor
    }

    override fun apply(file: PsiFile, warnings: List<MarkupLintWarning>, holder: AnnotationHolder) {
        logger.debug("Called apply")

        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        if (document == null) {
            logger.warn("Not Found document")
            return
        }

        warnings.forEach { warning ->
            run {
                // ある行のある列から末尾まで
                val endOffset: Int = document.getLineEndOffset(warning.line)
                val startOffset: Int = StringUtil.lineColToOffset(file.text, warning.line, warning.column)

                // See https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/844
                // TODO 本当に必要?
                if (startOffset !in 0..endOffset) {
                    logger.info("Skip negative text range")
                    return
                }

                val range = TextRange(startOffset, endOffset)

                // warningであることを示すアノテーションを作成
                holder.newAnnotation(HighlightSeverity.WARNING, warning.reason)
                    .range(range)
                    .create()

                logger.info("Create an annotation")
            }
        }
    }
}

