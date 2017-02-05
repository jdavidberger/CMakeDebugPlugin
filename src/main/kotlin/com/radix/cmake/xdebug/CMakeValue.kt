package com.radix.cmake.xdebug

import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace

class CMakeValue(debugger: CMakeDebuggerProxy, v: String) : XValue() {
    var value = v
    override fun computePresentation(node: XValueNode, place: XValuePlace) =
            node.setPresentation(null, "",  value, false)
}