package com.radix.cmake.config

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.radix.cmake.config.CMakeRunConfiguration

class CMakeRunConfigurationFactory(type : ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(p0: Project): RunConfiguration =
            CMakeRunConfiguration(p0, this, "")
}

