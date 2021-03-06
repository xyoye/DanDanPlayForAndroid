package com.xyoye.subtitle

import android.graphics.Color
import com.xyoye.subtitle.info.Caption

/**
 * Created by xyoye on 2020/12/15.
 */

object SubtitleUtils {

    /**
     * 文字转换显示需要字幕格式
     */
    fun caption2Subtitle(captionText: String?): MutableList<SubtitleText> {
        if (captionText.isNullOrEmpty())
            return mutableListOf()

        val newCaption = Caption()
        newCaption.content = captionText
        return caption2Subtitle(newCaption)
    }

    /**
     * caption转换显示需要字幕格式
     */
    fun caption2Subtitle(caption: Caption): MutableList<SubtitleText> {
        //字幕颜色
        val subtitleColor = getCaptionColor(caption.style?.color)

        //分割每行字幕
        val subtitle = caption.content.replace("<br />", "\n")
        val upperRegex = "\\N"
        val lowerRegex = "\n"
        if (subtitle.contains(upperRegex)) {
            return strings2Subtitle(subtitleColor, *(subtitle.split(upperRegex).toTypedArray()))
        }

        if (subtitle.contains(lowerRegex)) {
            return strings2Subtitle(subtitleColor, *(subtitle.split(lowerRegex).toTypedArray()))
        }

        return strings2Subtitle(subtitleColor, subtitle)
    }

    private fun strings2Subtitle(
        subtitleColor: Int,
        vararg subtitles: String
    ): MutableList<SubtitleText> {
        val subtitleList = mutableListOf<SubtitleText>()

        for (subtitle in subtitles) {
            //第一行以{开头，则认为是特殊字幕，现显示在顶部
            if (subtitle.startsWith("{")) {
                val endIndex = subtitle.lastIndexOf("}") + 1
                subtitleList.add(
                    if (endIndex != 0 && endIndex <= subtitle.length) {
                        //忽略{}中内容
                        SubtitleText(
                            subtitle.substring(
                                endIndex
                            ), true, subtitleColor
                        )
                    } else {
                        SubtitleText(
                            subtitle,
                            true,
                            subtitleColor
                        )
                    }
                )
            } else {
                subtitleList.add(
                    SubtitleText(
                        subtitle,
                        false,
                        subtitleColor
                    )
                )
            }
        }

        return subtitleList
    }


    /**
     * 获取字幕颜色
     */
    private fun getCaptionColor(colorStr: String?): Int {
        var rgbaText = colorStr

        if (rgbaText.isNullOrEmpty())
            return Color.WHITE

        if (!rgbaText.startsWith("#"))
            rgbaText = "#$colorStr"

        //颜色字符串为rgba格式，要转换成argb
        return try {
            val rgba = Color.parseColor(rgbaText)
            rgba ushr 8 or (rgba shl 32 - 8)
        } catch (e: IllegalArgumentException) {
            Color.WHITE
        }
    }
}