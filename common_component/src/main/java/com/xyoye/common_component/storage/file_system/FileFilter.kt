package com.xyoye.common_component.storage.file_system

import java.io.File
import java.io.FileFilter

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

interface FileFilter : FileFilter {

    override fun accept(file: File): Boolean

    fun file(file: File)
}