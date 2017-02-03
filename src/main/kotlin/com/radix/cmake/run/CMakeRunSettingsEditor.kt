package com.radix.cmake.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.radix.cmake.CMakeRunSettings
import javax.swing.JComponent
import javax.swing.JPanel

class CMakeRunSettingsEditor(project: Project, config: CMakeRunConfiguration) : SettingsEditor<CMakeRunConfiguration>() {
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

    override fun resetEditorFrom(config: CMakeRunConfiguration) {
   /*     config.port = 8080
        config.cmakePath = "cmake"
        config.sourceDir = ""
        config.workingDir = ""*/
    }
}