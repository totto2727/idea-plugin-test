package dev.totto2727.plugin.idea.test.fs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

object MarkupLintFileMatcher {
    // TODO Support other file extensions
    private val filePath = Regex("""^.+/.+\.html$""")

    private fun match(path: String) =
        path.matches(filePath)

    fun match(file: VirtualFile) = match(file.path)

    fun match(file: PsiFile) =
        // TODO null -> false collect?
        file.virtualFile?.let { match(it) } ?: false
}
