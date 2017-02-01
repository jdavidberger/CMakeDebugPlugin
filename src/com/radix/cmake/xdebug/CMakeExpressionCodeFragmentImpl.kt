package com.radix.cmake.xdebug

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.source.PsiPlainTextFileImpl
import com.intellij.testFramework.LightVirtualFile

class CMakeExpressionCodeFragmentImpl(project: Project, name: String, text: String) :
        PsiPlainTextFileImpl((PsiManager.getInstance(project) as PsiManagerEx).fileManager.createFileViewProvider(
        LightVirtualFile(name, StdFileTypes.PLAIN_TEXT, text), true)) {

    init {
        (viewProvider as SingleRootFileViewProvider).forceCachedPsi(this)
    }
}