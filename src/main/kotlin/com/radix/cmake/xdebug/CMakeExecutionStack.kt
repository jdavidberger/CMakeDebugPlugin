package com.radix.cmake.xdebug

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTextContainer
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import java.util.*
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.icons.AllIcons
import javax.swing.text.StyleConstants.setIcon



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

class CMakeStackFrame(debugger: CMakeDebuggerProxy, pos: SourceFilePosition, func : String) : XStackFrame() {
    var position = pos
    var debugger = debugger
    var function = func

    override fun getSourcePosition(): XSourcePosition? {
        return position
    }

    override fun getEvaluator(): XDebuggerEvaluator? {
        return CMakeDebugerEvaluator(debugger)
    }

    override fun customizePresentation(component: ColoredTextContainer) {
        val position = sourcePosition
        if (position != null) {
            component.append(function + " ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            component.append("(" + position.file.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            component.append(":" + (position.line + 1) + ")", SimpleTextAttributes.REGULAR_ATTRIBUTES)
            component.setIcon(AllIcons.Debugger.StackFrame)
        } else {
            component.append(XDebuggerBundle.message("invalid.frame"), SimpleTextAttributes.ERROR_ATTRIBUTES)
        }
    }
}

class CMakeDebugerEvaluator(debugger: CMakeDebuggerProxy) : XDebuggerEvaluator() {
    var debugger = debugger
    override fun evaluate(str: String, cb: XEvaluationCallback, location: XSourcePosition?) {
        debugger.evaluate(str, cb, location)
    }
}
