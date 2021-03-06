package com.xyoye.download_component.frostwire.download

import java.io.File

/**
 * Created by xyoye on 2020/12/31.
 *
 * 关于seed和peer
 *
 * 首先，以下都是我结合一些文档得出的自己的看法，不一定正确
 * 1.seed和peer都是代指其它客户端，只是两者状态不同
 * 2.seed是指已完成下载，正在作种的客户端；peer是指未完成下载的客户端
 * 3.更多的seed意味着更快的下载速度
 * 4.更多的peer有可能会减慢速度，因为它与当前客户端是竞争者关系，会同时消耗seed的带宽，
 *   但同时的它也在播种，客户端也可从peer客户端下载未完成的部分，从而提高速度。
 * 5.peer客户端下载完成后继续做种，即为seed
 */

interface Downloader {

    /**
     * 获取任务hash值
     */
    fun getInfoHash(): String

    /**
     * 获取通过任务信息生成的磁链URI
     */
    fun magnetUri(): String?

    /**
     * 获取当前已连接的其它客户端数量
     */
    fun getConnectedPeers(): Int

    /**
     * 获取所有可连接的其它客户端数量
     */
    fun getTotalPeers(): Int

    /**
     * 获取已连接的种子源数量
     */
    fun getConnectedSeeds(): Int

    /**
     * 获取可连接的种子源数量
     */
    fun getTotalSeeds(): Int

    /**
     * 多个文件时，返回文件所在的文件夹
     * 单个文件时，返回文件路径
     */
    fun getContentSavePath(): File?

    /**
     * 是否已暂停
     */
    fun isPaused(): Boolean

    /**
     * 是否正在做种中
     */
    fun isSeeding(): Boolean

    /**
     * 是否下载已完成
     */
    fun isFinished(): Boolean

    /**
     * 暂停任务
     */
    fun pause()

    /**
     * 恢复任务
     */
    fun resume()

    /**
     * 移除任务
     * @param deleteData 是否移除已下载数据
     */
    fun remove(deleteData: Boolean)

    /**
     * 返回下载集合中占主要内容的文件后缀
     * 未知文件返回: "torrent"
     */
    fun getPredominantFileExtension(): String?
}