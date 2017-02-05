package com.radix.cmake.xdebug

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.pom.Navigatable
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint

class SourceFilePosition() : XSourcePosition {
    var myLine : Int = 0
    var File = ""

    private val FILE_URL_PREFIX = "file:///"
    fun findFile(path: String): VirtualFile? {
        var path = path
        if ("" == path) {
            return null
        }

        if (!path.startsWith(FILE_URL_PREFIX)) {
            path = FILE_URL_PREFIX + path
        }

        return VirtualFileManager.getInstance().findFileByUrl(path)
    }

    override fun getFile(): VirtualFile = findFile(File)!!

    override fun getOffset(): Int = -1

    override fun getLine(): Int = myLine

    class NavImpl(p0 : Project, _file : VirtualFile, _line : Int) : Navigatable {
        var project = p0
        var file = _file
        var line = _line

        override fun navigate(requestFocus: Boolean) {
            OpenFileDescriptor(project, file, line, 0)
        }

        override fun canNavigate(): Boolean {
            return true
        }

        override fun canNavigateToSource(): Boolean {
            return true
        }
    }

    override fun createNavigatable(p0: Project): Navigatable {
        return NavImpl(p0, file, line)
    }

    constructor(line: Int, file: String) : this() {
        myLine = line
        File = file
    }

    constructor(loc: XLineBreakpoint<XBreakpointProperties<*>>) : this() {
        myLine = loc.line
        File = loc.presentableFilePath
    }
}