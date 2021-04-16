package com.xyoye.player.info

import android.graphics.Color
import com.xyoye.data_component.enums.PixelFormat
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.data_component.enums.SurfaceType
import com.xyoye.data_component.enums.VideoScreenScale

/**
 * Created by xyoye on 2020/10/29.
 */

object PlayerInitializer {

    var isPrintLog: Boolean = true
    var isOrientationEnabled = true
    var isEnableAudioFocus = true
    var isLooping = false
    var playerType = PlayerType.TYPE_IJK_PLAYER
    var surfaceType = SurfaceType.VIEW_TEXTURE
    var screenScale = VideoScreenScale.SCREEN_SCALE_DEFAULT

    var selectSourceDirectory: String? = null

    object IJKPlayer {
        var isMediaCodeCEnabled = false
        var isMediaCodeCH265Enabled = false
        var isOpenSLESEnabled = false
        var pixelFormat = PixelFormat.PIXEL_AUTO
    }

    object Danmu {
        var offsetPosition = 0L

        var size = 40
        var alpha = 100
        var stoke = 20
        var speed = 35
        var mobileDanmu = true
        var topDanmu = true
        var bottomDanmu = true
        var maxLine = -1
        var maxNum = 0
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