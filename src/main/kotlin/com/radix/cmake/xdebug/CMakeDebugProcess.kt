package com.radix.cmake.xdebug

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.radix.cmake.config.CMakeRunCommandLineState
import javax.swing.SwingUtilities.invokeLater


class CMakeDebugProcess(session: XDebugSession, state: CMakeRunCommandLineState,
                        serverProcessHandler: OSProcessHandler,
                        proxy : CMakeDebuggerProxy) : XDebugProcess(session), CMakeDebuggerListener, ProcessListener {


    private val serverProcessHandler = serverProcessHandler
    val proxy = proxy
    val myLineBreakpointHandler = CMakeLineBreakpointHandler(this)

    init {
        proxy.AddListener(this)
        serverProcessHandler.addProcessListener(this)
    }

    override fun getEditorsProvider(): XDebuggerEditorsProvider = CMakeDebuggerEditorsProvider()

    override fun sessionInitialized() {
        super.sessionInitialized()

        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "CMake debugger", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Connecting..."
                indicator.isIndeterminate = true

                try {
                    proxy.connect(indicator, serverProcessHandler, 60)

                    if (!proxy.isReady()) {
                        terminateDebug(null)
                    }

                    indicator.text = "Connected"
                    indicator.isIndeterminate = false
                } catch (e: Exception) {
                    terminateDebug(e.message)
                }

            }
        })
    }

    override fun doGetProcessHandler(): ProcessHandler? = serverProcessHandler

    override fun stop() {
        super.stop()
        processHandler.destroyProcess()
        proxy.shutdown()
    }

    private fun terminateDebug(msg: String?) {
        processHandler.destroyProcess()
        invokeLater({
            val text = "Debugger can't connect to CMake"
            Messages.showErrorDialog(if (msg != null) text + ":\r\n" + msg else text, "CMake debugger")
        })
    }

    override fun createConsole(): ExecutionConsole {
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(session.project)

        val console = consoleBuilder.console
        console.attachToProcess(serverProcessHandler)

        return console
    }

    override fun resume(context: XSuspendContext?) = proxy.resume()
    override fun startPausing() = proxy.pause()
    override fun startStepOver(context: XSuspendContext?) = proxy.stepOver()
    override fun startStepOut(context: XSuspendContext?) = proxy.stepOut()
    override fun startStepInto(context: XSuspendContext?) = proxy.stepInto()

    fun removeBreakPoint(sourceFile: SourceFilePosition) = proxy.removeBreakPoint(sourceFile)
    fun addBreakPoint(sourceFile: SourceFilePosition) = proxy.addBreakPoint(sourceFile)

    override fun getBreakpointHandlers(): Array<out XBreakpointHandler<*>> =
            arrayOf<XBreakpointHandler<*>>(myLineBreakpointHandler)

    override fun OnStateChange(newState: String, file: String, line: Int) {
        val bp = SourceFilePosition(line, file)
        val xBreakpoint = myLineBreakpointHandler.myBreakpointByPosition[bp]

        val ctx = CMakeSuspendContext(session.project, this)
        when(newState) {
            "Paused" -> {
                if(xBreakpoint != null)
                    session.breakpointReached(xBreakpoint,"", ctx)
                else
                    session.positionReached(ctx)
            }
            "Running" -> {
                session.sessionResumed()
            }
        }
    }

    override fun onTextAvailable(event: ProcessEvent?, outputType: Key<*>?) {

    }

    override fun processTerminated(event: ProcessEvent?) {
        processHandler.destroyProcess()
        proxy.shutdown()
        session.stop()
    }

    override fun processWillTerminate(event: ProcessEvent?, willBeDestroyed: Boolean) {

    }

    override fun startNotified(event: ProcessEvent?) {

    }
}


