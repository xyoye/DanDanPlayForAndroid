package com.xyoye.common_component.utils.meida

import com.xyoye.common_component.config.AppConfig

/**
 * Created by xyoye on 2024/2/4
 */

object VideoExtension {

    // 视频文件扩展名分隔符
    private const val SEPARATOR = ","

    // 默认支持的视频文件扩展名
    private val defaultSupport = arrayOf(
        "3gp", "asf", "asx", "avi",
        "dat", "flv", "m2ts", "m3u8",
        "m4s", "m4v", "mkv", "mov",
        "mp4", "mpe", "mpeg", "mpg",
        "rm", "rmvb", "vob", "wmv"
    )

    // 当前支持的视频文件扩展名
    private val _support = defaultSupport.toMutableList()
    val support: List<String> get() = _support

    // 当前支持的视频文件扩展名文本
    val supportText: String get() = _support.joinToString(SEPARATOR)

    init {
        refresh()
    }

    /**
     * 重置为默认支持的视频文件扩展名
     */
    fun resetDefault() {
        AppConfig.setSupportVideoExtension(defaultSupport.joinToString(SEPARATOR))
        refresh()
    }

    /**
     * 更新支持的视频文件扩展名
     */
    fun update(extensionText: String): Boolean {
        return update(extensionText.split(SEPARATOR))
    }

    /**
     * 更新支持的视频文件扩展名
     */
    fun update(extensions: List<String>): Boolean {
        val storeExtensions = extensions.filter { it.isNotBlank() }.map { it.lowercase() }
        if (storeExtensions.isEmpty()) {
            return false
        }
        AppConfig.setSupportVideoExtension(storeExtensions.joinToString(SEPARATOR))
        refresh()
        return true
    }

    /**
     * 是否是支持的视频文件扩展名
     */
    fun isSupport(extension: String): Boolean {
        return _support.contains(extension.lowercase())
    }

    /**
     * 从磁盘中获取支持的视频文件扩展名
     */
    private fun refresh() {
        val stored = (AppConfig.getSupportVideoExtension() ?: supportText).split(SEPARATOR)
        if (stored.isEmpty()) {
            return
        }
        _support.clear()
        _support.addAll(stored)
    }
}