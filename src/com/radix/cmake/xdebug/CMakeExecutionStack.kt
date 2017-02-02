package com.radix.cmake.xdebug

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import java.util.*

class CMakeExecutionStack() : XExecutionStack("") {
    var stack : List<SourceFilePosition> = ArrayList<SourceFilePosition>()

    constructor(_stack: List<SourceFilePosition>) : this() {
        stack = _stack
    }

    override fun getTopFrame(): XStackFrame? {
        if(stack.size > 0)
            return CMakeStackFrame(stack.get(0))
        return null
    }
    override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer?) {
        var frames = ArrayList<CMakeStackFrame>()
        for(i in firstFrameIndex..stack.size-1) {
            frames.add(CMakeStackFrame(stack.get(i)))
        }
        container?.addStackFrames(frames, true)
    }
}

class CMakeStackFrame(pos: SourceFilePosition) : XStackFrame() {
    var position = pos
    override fun getSourcePosition(): XSourcePosition? {
        return position
    }
}
