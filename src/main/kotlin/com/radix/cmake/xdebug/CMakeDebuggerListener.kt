package com.radix.cmake.xdebug

interface CMakeDebuggerListener {
    fun OnStateChange(newState: String, file: String, line: Int)
}