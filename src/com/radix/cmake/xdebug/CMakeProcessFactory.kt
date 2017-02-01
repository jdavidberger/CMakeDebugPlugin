package com.radix.cmake.xdebug

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.radix.cmake.run.CMakeRunConfiguration
import com.intellij.execution.configurations.GeneralCommandLine

class CMakeProcessFactory(debugPort: Int) {
    private var port : Int = 8080
    init {
        port = debugPort
    }
    constructor() : this(8080) {

    }

    fun create(config : CMakeRunConfiguration) : GeneralCommandLine {
        val result = GeneralCommandLine()

        result.exePath = "C:/Users/J/CLionProjects/CMake/build2015/bin/Debug/cmake.exe"

        result.setWorkDirectory(config.project.baseDir.path)
        result.addParameter("--debugger=" + config.getDebugPort())
        result.addParameter(config.project.baseDir.path)
        return result
    }

    @Throws(ExecutionException::class)
    fun createProcess(config: CMakeRunConfiguration): OSProcessHandler {
        val commandLine = create(config)
        val p = commandLine.createProcess()
        val cmdString = commandLine.commandLineString
        val handler = OSProcessHandler(p, cmdString)
        ProcessTerminatedListener.attach(handler)
        return handler
    }

}