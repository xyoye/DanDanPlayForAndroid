package com.xyoye.download_component.frostwire.download

import java.io.File

/**
 * Created by xyoye on 2020/12/31.
 */

interface TransferItem {

    /**
     *  名称
     */
    fun getName(): String

    /**
     *  显示名称
     */
    fun getDisplayName(): String

    /**
     *  真实文件
     */
    fun getFile(): File

    /**
     *  子任务数据大小
     */
    fun getSize(): Long

    /**
     *  是否已被忽略下载
     */
    fun isSkipped(): Boolean

    /**
     *  当前已下载数据大小
     */
    fun getDownloaded(): Long

    /**
     *  当前下载进度（0~100）
     */
    fun getProgress(): Int

    /**
     *  子任务是否已下载完成
     */
    fun isComplete(): Boolean
}