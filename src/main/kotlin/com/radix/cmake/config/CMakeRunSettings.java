package com.radix.cmake.config;

import javax.swing.*;

/**
 * Created by J on 2/1/2017.
 */
public class CMakeRunSettings {
    private JTextField debugPort;
    public JPanel RootPanel;
    private JTextField cmakeInstall;
    private JTextField srcDir;
    private JTextField buildDir;

    public int DebugPort() {
        return Integer.parseInt(debugPort.getText());
    }
    public void SetDebugPort(Integer port) {debugPort.setText(port.toString());}

    public String CMakeInstall() { return cmakeInstall.getText(); }
    public void SetCMakeInstall(String install) { cmakeInstall.setText(install); }

    public String SourceDir() { return srcDir.getText(); }
    public void SetSourceDir(String val) {srcDir.setText(val);}

    public String BuildDir() { return buildDir.getText(); }
    public void SetBuildDir(String val) { buildDir.setText(val); }

}
