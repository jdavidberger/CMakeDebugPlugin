package com.radix.cmake.xdebug

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider

class CMakeDebuggerEditorsProvider : XDebuggerEditorsProvider() {
    override fun createDocument(project: Project, text: String, sourcePosition: XSourcePosition?, mode: EvaluationMode): Document {
        val psiFile = CMakeExpressionCodeFragmentImpl(project, "CMakeDebugger.expr", text)
        return PsiDocumentManager.getInstance(project).getDocument(psiFile)!!
    }

    override fun getFileType(): FileType =
            CMakeFileType.INSTANCE
}

