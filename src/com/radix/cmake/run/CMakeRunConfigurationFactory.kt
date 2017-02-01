package com.radix.cmake.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener

class CMakeRunConfigurationFactory(type : ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(p0: Project): RunConfiguration {
        return CMakeRunConfiguration(p0, this, "")
    }
}

class CMakeRunConfiguration(project: Project, factory: CMakeRunConfigurationFactory, s: String) :
        ModuleBasedConfiguration<RunConfigurationModule> (s, RunConfigurationModule(project), factory) {
    override fun getValidModules(): MutableCollection<Module> {
        return mutableListOf(ModuleManager.getInstance(project).modules[0])
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return CMakeRunSettingsEditor(getConfigurationModule().getProject())
    }

    override fun getState(p0: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return CMakeRunCommandLineState(executionEnvironment, this);
    }
}

class CMakeRunSettingsEditor(project: Project) : SettingsEditor<CMakeRunConfiguration>() {

    private val myPanel = JPanel()

    override fun createEditor(): JComponent {
        return myPanel;
    }


    override fun applyEditorTo(p0: CMakeRunConfiguration) {
    }

    override fun resetEditorFrom(p0: CMakeRunConfiguration) {
    }
}

class CMakeRunCommandLineState(executionEnvironment: ExecutionEnvironment, cMakeRunConfiguration: CMakeRunConfiguration) : CommandLineState(executionEnvironment) {
    private fun create(): GeneralCommandLine {
        val result = GeneralCommandLine()

        result.exePath = "CMake.exe"

        result.addParameter("--debug=stdin")
        result.setWorkDirectory(".")

        return result
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = create()
        val p = commandLine.createProcess()
        val cmdString = commandLine.commandLineString
        val handler = OSProcessHandler(p, cmdString)
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
