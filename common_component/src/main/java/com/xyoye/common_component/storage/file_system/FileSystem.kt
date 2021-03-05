package com.xyoye.common_component.storage.file_system

import java.io.File
import java.io.InputStream

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

interface FileSystem {
    /**
     * 是否为文件夹
     */
    fun isDirectory(file: File): Boolean

    /**
     * 是否为文件
     */
    fun isFile(file: File): Boolean

    /**
     * 是否可写
     */
    fun canWrite(file: File): Boolean

    /**
     * 文件长度
     */
    fun length(file: File): Long

    /**
     * 最后更改时间
     */
    fun lastModified(file: File): Long

    /**
     * 文件是否存在
     */
    fun exists(file: File): Boolean

    /**
     * 创建文件夹
     */
    fun mkDirs(file: File): Boolean

    /**
     *  删除文件
     */
    fun delete(file: File): Boolean

    /**
     * 将文件扫描进系统
     */
    fun scan(file: File)

    /**
     * 复制文件
     */
    fun copy(src: File, dest: File): Boolean

    /**
     * 将数据写入文件
     */
    fun write(file: File, data: ByteArray): Boolean

    /**
     * 将数据写入文件
     */
    fun write(file: File, inputStream: InputStream, notClose : Boolean = false): Boolean

    /**
     * 将数据读出为字节数据
     */
    fun read(file: File): ByteArray?

    /**
     *  展开文件夹
     */
    fun listFiles(file: File, filter: FileFilter? = null) : Array<File>?

    /**
     * 遍历文件夹
     */
    fun walk(file: File, filter: FileFilter)

    /**
     * 获取文件句柄
     */
    fun openFd(file: File, mode: String): Int
}