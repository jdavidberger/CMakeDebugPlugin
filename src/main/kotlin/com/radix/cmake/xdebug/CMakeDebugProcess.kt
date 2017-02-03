package com.radix.cmake.xdebug

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.radix.cmake.run.CMakeRunCommandLineState
import javax.swing.SwingUtilities.invokeLater
import com.intellij.openapi.vfs.VirtualFileManager




class CMakeDebugProcess(session: XDebugSession, state: CMakeRunCommandLineState,
                        serverProcessHandler: OSProcessHandler,
                        proxy : CMakeDebuggerProxy) : XDebugProcess(session), CMakeDebuggerListener {

    private val serverProcessHandler = serverProcessHandler
    val proxy = proxy
    val myLineBreakpointHandler = CMakeLineBreakpointHandler(this)

    init {
        proxy.AddListener(this)
    }
    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return CMakeDebuggerEditorsProvider()
    }

    override fun sessionInitialized() {
        super.sessionInitialized()

        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "CMake debugger", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Connecting..."
                indicator.isIndeterminate = true

                try {
                    proxy.connect(indicator, serverProcessHandler, 60)

                    if (proxy.isReady()) {
                        //proxy.attach(myLineBreakpointHandler.myBreakpointByPosition. )
                    } else {
                        terminateDebug(null)
                    }

                } catch (e: Exception) {
                    terminateDebug(e.message)
                }

            }
        })
    }

    private fun terminateDebug(msg: String?) {
        processHandler.destroyProcess()
        invokeLater({
            val text = "Debugger can't connect to CMake"
            Messages.showErrorDialog(if (msg != null) text + ":\r\n" + msg else text, "Ant debugger")
        })
    }
    override fun createConsole(): ExecutionConsole {
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(session.project)

        val console = consoleBuilder.console
        console.attachToProcess(serverProcessHandler)

        return console
    }

    override fun resume(context: XSuspendContext?) {
        proxy.resume()
    }

    override fun startPausing() {
        proxy.pause();
    }

    override fun startStepOver() {
        proxy.stepOver()
    }

    override fun startStepOut() {
        proxy.stepOut()
    }

    override fun startStepInto() {
        proxy.stepInto()
    }

    fun  removeBreakPoint(sourceFile: SourceFilePosition) {
        proxy.removeBreakPoint(sourceFile)
    }
    fun  addBreakPoint(sourceFile: SourceFilePosition) {
        proxy.addBreakPoint(sourceFile)
    }

    override fun getBreakpointHandlers(): Array<out XBreakpointHandler<*>> {
        return arrayOf<XBreakpointHandler<*>>(myLineBreakpointHandler)
    }
    override fun OnStateChange(newState: String, file: String, line: Int) {
        var bp = SourceFilePosition(line, file)
        var xBreakpoint = myLineBreakpointHandler.myBreakpointByPosition[bp]

        var ctx = CMakeSuspendContext(session.project, this)
        when(newState) {
            "Paused" -> {
                if(xBreakpoint != null)
                    session.breakpointReached(xBreakpoint!!,"", ctx)
                else
                    session.positionReached(ctx)
            }
            "Running" -> {
                session.resume()
            }
        }
    }
}


class SourceFilePosition() : XSourcePosition {
    var myLine : Int = 0
    var File = ""

    init {}

    private val FILE_URL_PREFIX = "file:///"
    fun findFile(path: String): VirtualFile? {
        var path = path
        if (path == null || "" == path) {
            return null
        }

        if (!path.startsWith(FILE_URL_PREFIX)) {
            path = FILE_URL_PREFIX + path
        }

        return VirtualFileManager.getInstance().findFileByUrl(path)
    }

    override fun getFile(): VirtualFile {
        return findFile(File)!!
    }

    override fun getOffset(): Int {
        return -1
    }

    override fun getLine(): Int {
        return myLine
    }

    class NavImpl(p0 : Project, _file : VirtualFile, _line : Int) : Navigatable {
        var project = p0
        var file = _file
        var line = _line

        override fun navigate(requestFocus: Boolean) {
            com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, line, 0)
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

