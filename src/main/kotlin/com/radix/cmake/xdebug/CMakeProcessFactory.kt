package com.radix.cmake.xdebug

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.radix.cmake.config.CMakeRunConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import java.io.File

class CMakeProcessFactory {

    fun create(config : CMakeRunConfiguration) : GeneralCommandLine {
        val result = GeneralCommandLine()
        var workingdir = config.workingDir
        if(workingdir.isEmpty())
            workingdir = "cmake-debugger-build"

        if(!File(workingdir).isAbsolute)
            workingdir = config.project.baseDir.path + "/" + workingdir

        if(!File(workingdir).exists())
            File(workingdir).mkdir()

        result.exePath = "C:/Windows/System32/PING.EXE" //config.cmakePath

        result.setWorkDirectory( workingdir )
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