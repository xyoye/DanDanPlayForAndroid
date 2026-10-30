package com.xyoye.player.kernel.impl.vlc

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import com.xyoye.data_component.enums.VLCHWDecode
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.VlcProxyServer
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import java.io.File

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : VLC Media 及其关联文件描述符的创建结果
 *
 * @property media 已完成播放参数配置的 VLC Media
 * @property sourceFd 需要跟随对应 MediaPlayer 生命周期关闭的文件描述符
 */
internal data class VlcMediaHandle(
    val media: Media,
    val sourceFd: AssetFileDescriptor?
)

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : VLC 当前媒体源及其文件描述符所有权
 *
 * @property path 媒体播放地址
 * @property headers 媒体请求头
 * @property sourceFd 需要跟随对应 MediaPlayer 生命周期关闭的文件描述符
 */
internal data class VlcMediaSourceState(
    val path: String,
    val headers: Map<String, String>?,
    val sourceFd: AssetFileDescriptor?
)

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : VLC 外部音频或字幕轨道信息
 *
 * @property type VLC 外部轨道类型
 * @property path 外部轨道文件路径
 */
internal data class VlcExternalSlave(
    val type: Int,
    val path: String
)

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2026/10/29
 *    desc  : 根据播放地址创建并配置 VLC Media
 */
internal class VlcMediaFactory(
    private val context: Context,
    private val libVlc: LibVLC
) {

    /**
     * 创建具有独立资源所有权的 VLC Media。
     *
     * @param path 媒体播放地址
     * @param headers 媒体请求头
     * @param startPaused 是否在显示首帧后保持暂停
     * @return Media 创建结果，数据源无效或无法打开时返回 null
     */
    fun create(
        path: String,
        headers: Map<String, String>?,
        startPaused: Boolean = false
    ): VlcMediaHandle? {
        if (path.isEmpty()) {
            return null
        }

        val handle = createMedia(path, headers) ?: return null
        applyMediaOptions(handle.media, startPaused)
        return handle
    }

    /**
     * 根据播放地址创建 Media 和关联的文件描述符。
     *
     * @param path 媒体播放地址
     * @param headers 媒体请求头
     * @return Media 创建结果，数据源无法打开时返回 null
     */
    private fun createMedia(
        path: String,
        headers: Map<String, String>?
    ): VlcMediaHandle? {
        // VLC 播放器通过代理服务实现请求头设置
        if (headers?.isNotEmpty() == true) {
            val proxyServer = VlcProxyServer.getInstance()
            if (!proxyServer.isAlive) {
                proxyServer.start()
            }
            val proxyUrl = proxyServer.getInputStreamUrl(path, headers)
            return VlcMediaHandle(Media(libVlc, Uri.parse(proxyUrl)), null)
        }

        val videoUri = if (path.startsWith("/")) {
            Uri.fromFile(File(path))
        } else {
            Uri.parse(path)
        }

        // content 数据源需要保持文件描述符存活到对应 MediaPlayer 释放
        if (videoUri.scheme == ContentResolver.SCHEME_CONTENT) {
            val sourceFd = try {
                context.contentResolver.openAssetFileDescriptor(videoUri, "r")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: return null
            return VlcMediaHandle(Media(libVlc, sourceFd), sourceFd)
        }
        return VlcMediaHandle(Media(libVlc, videoUri), null)
    }

    /**
     * 应用媒体硬解配置和恢复播放选项。
     *
     * @param media 需要配置的 VLC Media
     * @param startPaused 是否在显示首帧后保持暂停
     */
    private fun applyMediaOptions(media: Media, startPaused: Boolean) {
        when (PlayerInitializer.Player.vlcHWDecode) {
            // 完全关闭 VLC 硬件解码
            VLCHWDecode.HW_ACCELERATION_DISABLE -> media.setHWDecoderEnabled(false, false)
            // 仅使用硬件解码，不启用直接渲染
            VLCHWDecode.HW_ACCELERATION_DECODING -> {
                media.setHWDecoderEnabled(true, true)
                media.addOption(":no-mediacodec-dr")
                media.addOption(":no-omxil-dr")
            }
            // 启用完整硬件加速
            VLCHWDecode.HW_ACCELERATION_FULL -> media.setHWDecoderEnabled(true, true)
            // 自动模式沿用 libVLC 默认配置
            else -> Unit
        }

        // 由 libVLC 在首帧呈现后暂停，避免 Java 层过早 pause 导致黑屏
        if (startPaused) {
            media.addOption(":start-paused")
        }
    }
}
