package com.radix.cmake.config
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.radix.cmake.config.CMakeRunConfigurationFactory
import java.awt.Image.SCALE_SMOOTH
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

class CMakeConfigType : ConfigurationType {
    override fun getIcon(): Icon =
            ImageIcon(ImageIO.read( this.javaClass.classLoader.getResource( "cmake-icon.png" ) ).getScaledInstance(16, 16, SCALE_SMOOTH))

    override fun getConfigurationTypeDescription(): String = "CMake configure and generate"

    override fun getId(): String = "CMakeDebugRunner"

    override fun getDisplayName(): String = "CMake"

    private val myFactory: ConfigurationFactory = CMakeRunConfigurationFactory(this)

    override fun getConfigurationFactories(): Array<out ConfigurationFactory> = arrayOf( myFactory)

}