package com.radix.cmake.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.radix.cmake.CMakeRunSettings
import javax.swing.JComponent
import javax.swing.JPanel

class CMakeRunSettingsEditor(project: Project) : SettingsEditor<CMakeRunConfiguration>() {

    private val myPanel = CMakeRunSettings()

    override fun createEditor(): JComponent {
        return myPanel.RootPanel;
    }


    override fun applyEditorTo(p0: CMakeRunConfiguration) {
    }

    override fun resetEditorFrom(p0: CMakeRunConfiguration) {
    }
}