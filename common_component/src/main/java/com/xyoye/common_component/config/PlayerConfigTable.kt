package com.xyoye.common_component.config

import com.xyoye.data_component.enums.PixelFormat
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.data_component.enums.VLCHWDecode
import com.xyoye.data_component.enums.VLCPixelFormat
import com.xyoye.mmkv_annotation.MMKVFiled
import com.xyoye.mmkv_annotation.MMKVKotlinClass

@MMKVKotlinClass(className = "PlayerConfig")
object PlayerConfigTable {
    //是否允许屏幕旋转
    @MMKVFiled
    const val allowOrientationChange = true

    //是否使用surface view
    @MMKVFiled
    const val useSurfaceView = false

    //是否使用硬解码
    @MMKVFiled
    const val useMediaCodeC = false

    //是否使用h265硬解码
    @MMKVFiled
    const val useMediaCodeCH265 = false

    //是否使用OpenSLES
    @MMKVFiled
    const val useOpenSlEs = false

    //使用播放器类型
    @MMKVFiled
    val usePlayerType = PlayerType.TYPE_VLC_PLAYER.value

    //使用播放器像素格式
    @MMKVFiled
    val usePixelFormat = PixelFormat.PIXEL_AUTO.value

    //VLC内核像素格式
    @MMKVFiled
    val useVLCPixelFormat = VLCPixelFormat.PIXEL_RGB_32.value

    //VLC内核硬件加速
    @MMKVFiled
    val useVLCHWDecoder = VLCHWDecode.HW_ACCELERATION_AUTO.value

    //视频倍速
    @MMKVFiled
    val newVideoSpeed = 1f

    //自动播放下一集
    @MMKVFiled
    val autoPlayNext = true
}