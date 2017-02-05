package com.radix.cmake.xdebug

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XSuspendContext

class CMakeSuspendContext(project: Project, debugProcess: CMakeDebugProcess) : XSuspendContext() {
    private var myExecutionStack: CMakeExecutionStack? = null

    init {
        val debuggerProxy = debugProcess.proxy
        if (debuggerProxy.isReady()) {
            myExecutionStack = debuggerProxy.GetLastBacktrace()
        }
    }

    override fun getActiveExecutionStack(): XExecutionStack? = myExecutionStack

    override fun getExecutionStacks(): Array<XExecutionStack> {
        if(myExecutionStack == null)
            return arrayOf()
        return arrayOf(myExecutionStack!!)
    }

}