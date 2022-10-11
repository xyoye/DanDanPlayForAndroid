package com.xyoye.player.info

import android.graphics.Color
import com.xyoye.data_component.enums.*

/**
 * Created by xyoye on 2020/10/29.
 */

object PlayerInitializer {

    var isPrintLog: Boolean = true
    var isOrientationEnabled = true
    var isEnableAudioFocus = true
    var isLooping = false
    var playerType = PlayerType.TYPE_VLC_PLAYER
    var surfaceType = SurfaceType.VIEW_TEXTURE
    var screenScale = VideoScreenScale.SCREEN_SCALE_DEFAULT

    var selectSourceDirectory: String? = null

    object Player {
        const val DEFAULT_SPEED = 25

        var isMediaCodeCEnabled = false
        var isMediaCodeCH265Enabled = false
        var isOpenSLESEnabled = false
        var pixelFormat = PixelFormat.PIXEL_AUTO
        var vlcPixelFormat = VLCPixelFormat.PIXEL_RGB_32
        var vlcHWDecode = VLCHWDecode.HW_ACCELERATION_AUTO
        var videoSpeed = 1f
        var isAutoPlayNext = true
    }

    object Danmu {
        const val DEFAULT_POSITION = 0L
        const val DEFAULT_SIZE = 40
        const val DEFAULT_ALPHA = 100
        const val DEFAULT_STOKE = 20
        const val DEFAULT_SPEED = 35
        const val DEFAULT_MOBILE_ENABLE = true
        const val DEFAULT_TOP_ENABLE = true
        const val DEFAULT_BOTTOM_ENABLE = true
        const val DEFAULT_MAX_LINE = -1
        const val DEFAULT_MAX_NUM = 0

        var offsetPosition = DEFAULT_POSITION
        var size = DEFAULT_SIZE
        var alpha = DEFAULT_ALPHA
        var stoke = DEFAULT_STOKE
        var speed = DEFAULT_SPEED
        var mobileDanmu = DEFAULT_MOBILE_ENABLE
        var topDanmu = DEFAULT_TOP_ENABLE
        var bottomDanmu = DEFAULT_BOTTOM_ENABLE
        var maxLine = DEFAULT_MAX_LINE
        var maxNum = DEFAULT_MAX_NUM
        var cloudBlock = false
        var updateInChoreographer = true
    }

    object Subtitle {
        var offsetPosition = 0L

        var textSize = 20
        var strokeWidth = 5
        var textColor = Color.WHITE
        var strokeColor = Color.BLACK
    }
}