package com.radix.cmake.xdebug

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.radix.cmake.run.CMakeRunCommandLineState


class CMakeDebugProcess(session: XDebugSession, state: CMakeRunCommandLineState,
                        serverProcessHandler: OSProcessHandler, proxy : CMakeDebuggerProxy) : XDebugProcess(session) {

    private val serverProcessHandler = serverProcessHandler
    private val proxy = proxy

    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return CMakeDebuggerEditorsProvider()
    }



    override fun createConsole(): ExecutionConsole {
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(session.project)

        val console = consoleBuilder.console
        console.attachToProcess(serverProcessHandler)
        serverProcessHandler.startNotify()
        proxy.startClientThread()
        return console
    }
}