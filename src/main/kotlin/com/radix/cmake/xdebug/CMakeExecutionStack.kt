package com.radix.cmake.xdebug

import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import java.util.*


class CMakeExecutionStack(debugger: CMakeDebuggerProxy) : XExecutionStack("") {
    var stack : List<CMakeStackFrame> = ArrayList<CMakeStackFrame>()
    var debugger = debugger

    constructor(debugger: CMakeDebuggerProxy, _stack: List<CMakeStackFrame>) : this(debugger) {
        stack = _stack
    }

    override fun getTopFrame(): XStackFrame? {
        if(stack.size > 0)
            return stack.get(0)
        return null
    }
    override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer?) {
        var frames = ArrayList<CMakeStackFrame>()
        for(i in firstFrameIndex..stack.size-1) {
            frames.add( stack.get(i))
        }
        container?.addStackFrames(frames, true)
    }

}

class CMakeDebugerEvaluator(debugger: CMakeDebuggerProxy) : XDebuggerEvaluator() {
    var debugger = debugger
    override fun evaluate(str: String, cb: XEvaluationCallback, location: XSourcePosition?) {
        debugger.evaluate(str, cb, location)
    }
}
