package dev.totto2727.plugin.idea.test.annotator

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import dev.totto2727.plugin.idea.test.annotator.violation.MarkupLintViolation
import dev.totto2727.plugin.idea.test.fs.MarkupLintFileMatcher
import java.io.File
import kotlin.io.path.pathString

class MarkupLintAnnotator : ExternalAnnotator<MarkupLintAnnotator.InitialInfo, MarkupLintAnnotator.AnnotationResult>() {
    val logger = Logger.getInstance(javaClass)

    data class InitialInfo(
        val projectDirectory: String,
        val workingDirectory: String,
        val filename: String,
    )

    data class AnnotationResult(
        val violations: List<MarkupLintViolation>,
    )

    override fun collectInformation(file: PsiFile): InitialInfo? {
        if (!MarkupLintFileMatcher.match(file)) return null

        val projectDirectory = file.project.basePath ?: return null

        return file.virtualFile.let {
            InitialInfo(
                projectDirectory = projectDirectory,
                workingDirectory = it.toNioPath().parent.pathString,
                filename = it.name,
            )
        }.also { logger.info("collectInformation: $it") }
    }

    override fun doAnnotate(collectedInfo: InitialInfo?): AnnotationResult? {
        if (collectedInfo == null) return null

        val (projectDirectory, workingDirectory, filename) = collectedInfo

        val process =
            ProcessBuilder(
                listOf(
                    "$projectDirectory/./node_modules/.bin/markuplint",
                    "--format",
                    "JSON",
                    "$workingDirectory/$filename"
                )
            ).apply {
                directory(File(projectDirectory))
            }.start().apply { waitFor() }

        return AnnotationResult(
            violations = MarkupLintViolation.fromJson(process.inputStream),
        ).also { logger.info("doAnnotate: $it") }
    }

    override fun apply(file: PsiFile, annotationResult: AnnotationResult?, holder: AnnotationHolder) {
        if (annotationResult == null) return

        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return

        annotationResult.violations.forEach {
            holder
                .newAnnotation(it.severity.toHighlightSeverity(), "${it.ruleId}: ${it.message}")
                .highlightType(ProblemHighlightType.GENERIC_ERROR)
                .range(
                    TextRange.from(
                        document.getLineStartOffset(it.lineZeroBase) + it.colZeroBase,
                        it.raw.length,
                    )
                )
                .create()
        }
    }
}
