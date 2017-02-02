package com.radix.cmake.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class CMakeRunConfigurationFactory(type : ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(p0: Project): RunConfiguration {
        return CMakeRunConfiguration(p0, this, "")
    }
}

