package com.radix.cmake.run
import com.intellij.execution.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import javax.swing.Icon

class CMakeConfigType :ConfigurationType {
    override fun getIcon(): Icon {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConfigurationTypeDescription(): String {
        return "CMake configure and generate"
    }

    override fun getId(): String {
        return "com.radix.cmake"
    }

    override fun getDisplayName(): String {
        return "CMake";
    }

    override fun getConfigurationFactories(): Array<out ConfigurationFactory> {
        return arrayOf();
    }

}