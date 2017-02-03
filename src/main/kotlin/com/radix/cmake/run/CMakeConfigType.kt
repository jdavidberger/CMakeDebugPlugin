package com.radix.cmake.run
import com.intellij.execution.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import java.awt.Image.SCALE_SMOOTH
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

class CMakeConfigType :ConfigurationType {
    override fun getIcon(): Icon {
        return ImageIcon(ImageIO.read( this.javaClass.classLoader.getResource( "cmake-icon.png" ) ).getScaledInstance(16, 16, SCALE_SMOOTH))
    }

    override fun getConfigurationTypeDescription(): String {
        return "CMake configure and generate"
    }

    override fun getId(): String {
        return "CMakeDebugRunner"
    }

    override fun getDisplayName(): String {
        return "CMake"
    }

    private val myFactory: ConfigurationFactory = CMakeRunConfigurationFactory(this)

    override fun getConfigurationFactories(): Array<out ConfigurationFactory> {
        return arrayOf( myFactory)
    }

}