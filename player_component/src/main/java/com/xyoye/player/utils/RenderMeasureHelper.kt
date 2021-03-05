package com.xyoye.player.utils

import android.view.View
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.info.PlayerInitializer

/**
 * Created by xyoye on 2020/11/3.
 */

class RenderMeasureHelper {
    var mVideoDegree = 0
    var mScreenScale = PlayerInitializer.screenScale
    var mVideoWidth: Int = 0
    var mVideoHeight: Int = 0

    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {

        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        // 软解码时处理旋转信息，交换宽高
        if (mVideoDegree == 90 || mVideoDegree == 270) {
            widthSpec += heightSpec
            heightSpec = widthSpec - heightSpec
            widthSpec -= heightSpec
        }

        var width = View.MeasureSpec.getSize(widthSpec)
        var height = View.MeasureSpec.getSize(heightSpec)

        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return intArrayOf(width, height)
        }

        when (mScreenScale) {
            VideoScreenScale.SCREEN_SCALE_ORIGINAL -> {
                width = mVideoWidth
                height = mVideoHeight
            }
            VideoScreenScale.SCREEN_SCALE_16_9 -> {
                if (height > width / 16 * 9) {
                    height = width / 16 * 9
                } else {
                    width = height / 9 * 16
                }
            }
            VideoScreenScale.SCREEN_SCALE_4_3 -> {
                if (height > width / 4 * 3) {
                    height = width / 4 * 3
                } else {
                    width = height / 3 * 4
                }
            }
            VideoScreenScale.SCREEN_SCALE_MATCH_PARENT -> {
                width = widthSpec
                height = heightSpec
            }
            VideoScreenScale.SCREEN_SCALE_CENTER_CROP -> {
                if (mVideoWidth * height > width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight
                } else {
                    height = width * mVideoHeight / mVideoWidth
                }
            }
            else -> {
                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        }
        return intArrayOf(width, height)
    }
}