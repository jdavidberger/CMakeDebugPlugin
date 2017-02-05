package com.radix.cmake.xdebug

import java.util.*

open class CMakeDebuggerListenerHub : CMakeDebuggerListener {
    var listeners = ArrayList<CMakeDebuggerListener>()

    fun AddListener(listener : CMakeDebuggerListener) {
        listeners.add(listener)
    }

    override fun OnStateChange(newState: String, file: String, line: Int) {
        for (obj in listeners) {
            obj.OnStateChange(newState, file, line)
        }
    }

}