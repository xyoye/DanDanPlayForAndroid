package com.xyoye.common_component.utils.aliyun

import com.taobao.update.apk.ApkDownloadListener

/**
 * Created by xyoye on 2022/10/25.
 */

class UpdateProgressObserver(private val block: (Int) -> Unit) : ApkDownloadListener {
    override fun onPreDownload() {

    }

    override fun onDownloadProgress(p0: Int) {
        block.invoke(p0)
    }

    override fun onStartFileMd5Valid(p0: String?, p1: String?) {

    }

    override fun onFinishFileMd5Valid(p0: Boolean) {

    }

    override fun onDownloadFinish(p0: String?, p1: String?) {

    }

    override fun onDownloadError(p0: String?, p1: Int, p2: String?) {

    }
}