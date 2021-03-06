package com.xyoye.download_component.frostwire.io

import com.frostwire.jlibtorrent.swig.posix_stat_t
import com.frostwire.jlibtorrent.swig.posix_wrapper
import com.xyoye.common_component.storage.file_system.LollipopFileSystem
import com.xyoye.common_component.utils.DDLog
import java.io.File

/**
 * Created by xyoye on 2020/12/30.
 */

class PosixCalls(private val fileSystem: LollipopFileSystem) : posix_wrapper() {

    override fun open(path: String?, flags: Int, mode: Int): Int {
        DDLog.i("posix - open: $path")

        var fd = super.open(path, flags, mode)
        if (fd >= 0) {
            return fd
        }
        path ?: return fd

        fd = fileSystem.openFd(File(path), "rw")
        if (fd < 0) {
            DDLog.i("posix wrapper failed to create native fd for: $path")
        }
        return fd
    }

    override fun stat(path: String?, buf: posix_stat_t?): Int {
        DDLog.i("posix - stat: $path")

        val result = super.stat(path, buf)
        if (result >= 0) {
            return result
        }
        path ?: return result

        val file = File(path)

        val modeIsDir = if (fileSystem.isDirectory(file)) 0x0040000 else 0x0
        val modeIfReg = 0x0100000
        buf?.apply {
            mode = modeIsDir or modeIfReg
            size = fileSystem.length(file)
            val time = fileSystem.lastModified(file) / 1000
            atime = time
            mtime = time
            ctime = time
        }
        return 0
    }

    override fun mkdir(path: String?, mode: Int): Int {
        DDLog.i("posix - mkdir: $path")

        var result = super.mkdir(path, mode)
        if (result >= 0) {
            return result
        }
        path ?: return result

        result = if (fileSystem.mkDirs(File(path))) 0 else -1
        if (result < 0) {
            DDLog.i("posix wrapper failed to create dir: $path")
        }
        return result
    }

    override fun rename(oldpath: String?, newpath: String?): Int {
        DDLog.i("posix - rename:$oldpath -> $newpath")

        val result = super.rename(oldpath, newpath)
        if (result >= 0)
            return result

        oldpath ?: return result
        newpath ?: return result

        val srcFile = File(oldpath)
        return if (fileSystem.copy(srcFile, File(newpath))) {
            fileSystem.delete(srcFile)
            0
        } else {
            DDLog.i("posix wrapper failed to copy file: $oldpath -> $newpath")
            -1
        }
    }

    override fun remove(path: String?): Int {
        DDLog.i("posix - remove:$path")
        var result = super.remove(path)
        if (result >= 0) {
            return result
        }
        path ?: return result

        result = if (fileSystem.delete(File(path))) 0 else -1
        if (result < 0) {
            DDLog.i("posix wrapper failed to delete file: $path")
        }
        return result
    }
}