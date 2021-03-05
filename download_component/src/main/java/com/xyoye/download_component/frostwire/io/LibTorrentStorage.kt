package com.xyoye.download_component.frostwire.io

import com.frostwire.jlibtorrent.swig.libtorrent
import com.xyoye.common_component.storage.file_system.LollipopFileSystem
import com.xyoye.common_component.storage.platform.AndroidPlatform

/**
 * Created by xyoye on 2020/12/30.
 */

object LibTorrentStorage {
    fun adaptAndroidFileSystem(){
        val fileSystem = AndroidPlatform.getInstance().getFileSystem()
        val posixCalls = PosixCalls(fileSystem)
        posixCalls.swigReleaseOwnership()
        libtorrent.set_posix_wrapper(posixCalls)
    }
}