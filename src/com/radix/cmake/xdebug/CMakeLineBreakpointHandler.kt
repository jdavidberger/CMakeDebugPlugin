package com.radix.cmake.xdebug

import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import java.util.*

class CMakeLineBreakpointHandler(debugProcess : CMakeDebugProcess) :
        XBreakpointHandler<XLineBreakpoint<XBreakpointProperties<*>>>(CMakeBreakpointType::class.java)
{
    private val myDebugProcess = debugProcess
    var myBreakpointByPosition = HashMap<SourceFilePosition, XLineBreakpoint<*>>()

    override fun unregisterBreakpoint(xBreakpoint: XLineBreakpoint<XBreakpointProperties<*>>, temporary: Boolean) {
        var breakpoint = SourceFilePosition(xBreakpoint)
        myDebugProcess.removeBreakPoint(breakpoint)
        myBreakpointByPosition.remove(breakpoint)

    }

    override fun registerBreakpoint(xBreakpoint: XLineBreakpoint<XBreakpointProperties<*>>) {
        var breakpoint = SourceFilePosition(xBreakpoint)
        myDebugProcess.addBreakPoint(breakpoint)
        myBreakpointByPosition.put(breakpoint, xBreakpoint)
    }

}