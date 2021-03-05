package com.xyoye.download_component.frostwire.download

import com.xyoye.download_component.frostwire.utils.TransferState
import java.io.File
import java.util.*

/**
 * Created by xyoye on 2020/12/31.
 */

interface Transfer {
    /**
     *  任务名
     */
    fun getName(): String?

    /**
     *  当子任务只有一个时，返回子文件名
     *  否则返回任务名
     */
    fun getDisplayName(): String

    /**
     *  获取下载路径
     */
    fun getSavePath(): File

    /**
     *  获取预览文件
     */
    fun previewFile(): File?

    /**
     *  获取任务总下载大小
     */
    fun getSize(): Long

    /**
     *  任务创建时间
     */
    fun getCreateDate(): Date

    /**
     *  任务当前状态
     */
    fun getState(): TransferState

    /**
     *  当前已下载数据量大小
     */
    fun getBytesReceived(): Long

    /**
     *  当前已上传数据量大小
     */
    fun getBytesSent(): Long

    /**
     *  当前下载速度
     */
    fun getDownloadSpeed(): Long

    /**
     *  当前上传速度
     */
    fun getUploadSpeed(): Long

    /**
     *  距离任务完成所需的时间
     */
    fun getETA(): Long

    /**
     *  任务当前下载进度（0~100）
     */
    fun getProgress(): Int

    /**
     *  任务是否正在下载
     */
    fun isDownloading(): Boolean

    /**
     *  任务是否已完成
     */
    fun isComplete(): Boolean

    /**
     *  获取子任务
     */
    fun getItems(): MutableList<TransferItem>
}