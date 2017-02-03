package com.radix.cmake.xdebug

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.radix.cmake.run.CMakeRunConfiguration
import com.intellij.execution.configurations.GeneralCommandLine

class CMakeProcessFactory {

    fun create(config : CMakeRunConfiguration) : GeneralCommandLine {
        val result = GeneralCommandLine()

        result.exePath = config.cmakePath

        result.setWorkDirectory(config.workingDir)
        result.addParameter("--debugger=" + config.port)
        result.addParameter( if(config.sourceDir.isEmpty()) config.project.baseDir.path else config.sourceDir)
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