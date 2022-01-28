package com.xyoye.local_component.listener

/**
 * Created by xyoye on 2020/10/20.
 */

interface ExtraSourceListener {
    fun search(searchText: String)

    fun setting()

    fun localFile()

    fun unbindDanmu()

    fun unbindSubtitle()
}