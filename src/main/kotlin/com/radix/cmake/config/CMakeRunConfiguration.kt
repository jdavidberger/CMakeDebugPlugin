package com.radix.cmake.config

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
import com.intellij.openapi.util.JDOMExternalizerUtil
import org.jdom.Element
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.javaType

class CMakeRunConfiguration(project: Project, factory: CMakeRunConfigurationFactory, s: String) :
        ModuleBasedConfiguration<RunConfigurationModule>(s, RunConfigurationModule(project), factory) {

    var cmakePath = "cmake"
    var port = 8080
    var workingDir = ""
    var sourceDir = ""

    override fun getValidModules(): MutableCollection<Module> =
            mutableListOf(ModuleManager.getInstance(project).modules[0])

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
            CMakeRunSettingsEditor(this)

    override fun getState(p0: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? =
            CMakeRunCommandLineState(executionEnvironment, this)

    override fun writeExternal(element: Element?) {
        super.writeExternal(element)
        if (element != null) {
            CMakeRunConfiguration::class.members
                    .filterIsInstance<KMutableProperty<*>>()
                    .forEach {
                        JDOMExternalizerUtil.writeField(element, it.name, it.getter.call(this).toString())
                    }
        }
    }

    override fun readExternal(element: Element?) {
        super.writeExternal(element)
        if(element != null) {
            CMakeRunConfiguration::class.members
                    .filterIsInstance<KMutableProperty<*>>()
                    .forEach {
                        if(it.returnType.javaType.typeName == "int")
                            it.setter.call(this, (JDOMExternalizerUtil.readField(element, it.name) ?: "0").toInt())
                        else if(it.returnType.javaType.typeName == "java.lang.String")
                            it.setter.call(this, (JDOMExternalizerUtil.readField(element, it.name) ?: "") )
                    }
        }
    }
}