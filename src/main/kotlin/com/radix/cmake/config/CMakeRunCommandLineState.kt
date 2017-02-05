package com.radix.cmake.config

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.radix.cmake.config.CMakeRunConfiguration
import com.radix.cmake.xdebug.CMakeProcessFactory
import org.jetbrains.annotations.NotNull


class CMakeRunCommandLineState(executionEnvironment: ExecutionEnvironment, config: CMakeRunConfiguration) :
        CommandLineState(executionEnvironment) {
    var myConfig = config

    override fun startProcess(): OSProcessHandler = CMakeProcessFactory().createProcess(myConfig)

    fun createAttachedConsole(project: Project, processHandler: ProcessHandler): ConsoleView {
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)

        val console = consoleBuilder.console
        console.attachToProcess(processHandler)
        return console
    }

    override fun execute(@NotNull executor: Executor, @NotNull runner: ProgramRunner<*>): ExecutionResult {
        val processHandler = startProcess()
        val console = createAttachedConsole(myConfig.project, processHandler)

        return DefaultExecutionResult(console, processHandler, *createActions(console, processHandler))
    }
}