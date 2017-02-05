package com.radix.cmake.xdebug

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Image.SCALE_SMOOTH
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

open class CMakeFileType : FileType {
    object INSTANCE : CMakeFileType()

    override fun getIcon(): Icon? =
            ImageIcon(ImageIO.read(this.javaClass.classLoader.getResource("cmake-icon.png")).getScaledInstance(16, 16, SCALE_SMOOTH))

    override fun getName(): String = "CMake"

    override fun isBinary(): Boolean = false

    override fun isReadOnly(): Boolean = false

    override fun getDefaultExtension(): String = "cmake"

    override fun getCharset(file: VirtualFile, content: ByteArray): String? = "utf8"

    override fun getDescription(): String = "CMake script"
}