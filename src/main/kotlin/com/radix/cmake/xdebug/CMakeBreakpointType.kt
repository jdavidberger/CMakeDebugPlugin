package com.radix.cmake.xdebug

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.openapi.vfs.VirtualFile


class CMakeBreakpointType : XLineBreakpointType<XBreakpointProperties<*>>("cmake-line", "CMake breakpoints") {
    override fun createBreakpointProperties(virtualFile: VirtualFile, i: Int): XBreakpointProperties<*>? =
            null

    override fun canPutAt(file: VirtualFile, line: Int, project: Project): Boolean =
            "cmake" == file.extension || "CMakeLists.txt" == file.name
}
