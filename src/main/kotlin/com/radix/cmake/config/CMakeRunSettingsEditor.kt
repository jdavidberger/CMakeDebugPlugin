package com.radix.cmake.config

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.radix.cmake.config.CMakeRunSettings
import com.radix.cmake.config.CMakeRunConfiguration
import javax.swing.JComponent
import javax.swing.JPanel

class CMakeRunSettingsEditor(config: CMakeRunConfiguration) : SettingsEditor<CMakeRunConfiguration>() {
    var config = config
    private val myPanel = CMakeRunSettings()

    override fun createEditor(): JComponent {
        myPanel.SetDebugPort(config.port)
        myPanel.SetCMakeInstall(config.cmakePath)
        myPanel.SetBuildDir(config.workingDir)
        myPanel.SetSourceDir(config.sourceDir)
        return myPanel.RootPanel;
    }

    override fun applyEditorTo(config: CMakeRunConfiguration) {
        config.port = myPanel.DebugPort()
        config.cmakePath = myPanel.CMakeInstall()
        config.sourceDir = myPanel.SourceDir()
        config.workingDir = myPanel.BuildDir()
    }

    override fun resetEditorFrom(incomingConfig: CMakeRunConfiguration) {
        config = incomingConfig
    }
}