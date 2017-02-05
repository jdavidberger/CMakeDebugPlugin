package com.radix.cmake.xdebug

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XStackFrame

class CMakeStackFrame(debugger: CMakeDebuggerProxy, pos: SourceFilePosition, func : String) : XStackFrame() {
    var position = pos
    var debugger = debugger
    var function = func

    override fun getSourcePosition(): XSourcePosition? = position

    override fun getEvaluator(): XDebuggerEvaluator? = CMakeDebugerEvaluator(debugger)

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