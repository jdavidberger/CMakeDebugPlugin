package com.radix.cmake.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationModule
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class CMakeRunConfiguration(project: Project, factory: CMakeRunConfigurationFactory, s: String) :
        ModuleBasedConfiguration<RunConfigurationModule>(s, RunConfigurationModule(project), factory) {

    var cmakePath = "cmake"
    var port = 8080
    var workingDir = ""
    var sourceDir = ""

    override fun getValidModules(): MutableCollection<Module> {
        return mutableListOf(ModuleManager.getInstance(project).modules[0])
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = CMakeRunSettingsEditor(configurationModule.project, this)

    override fun getState(p0: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
        return CMakeRunCommandLineState(executionEnvironment, this)
    }

}