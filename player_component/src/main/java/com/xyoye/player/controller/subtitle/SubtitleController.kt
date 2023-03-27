package com.xyoye.player.controller.subtitle

import android.content.Context
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.wrapper.InterSubtitleController
import com.xyoye.subtitle.MixedSubtitle
import com.xyoye.subtitle.SubtitleType

/**
 * Created by xyoye on 2021/4/15.
 */

class SubtitleController(context: Context) : InterSubtitleController {
    private val subtitleTextView = SubtitleTextView(context)
    private val subtitleImageView = SubtitleImageView(context)
    private val externalSubtitleView = ExternalSubtitleView(context)

    private val subtitleViews: Array<InterControllerView> = arrayOf(
        subtitleTextView,
        subtitleImageView,
        externalSubtitleView
    )

    override fun addSubtitleStream(filePath: String) {
        externalSubtitleView.addSubtitleStream(filePath)
    }

    override fun updateSubtitleOffsetTime() {
        externalSubtitleView.updateOffsetTime()
    }

    override fun getExternalSubtitleStream(): List<VideoStreamBean> {
        return externalSubtitleView.getExternalSubtitleStream()
    }

    override fun selectSubtitleStream(stream: VideoStreamBean) {
        externalSubtitleView.selectSubtitleStream(stream)
    }

    override fun updateTextSize() {
        subtitleTextView.updateTextSize()
    }

    override fun updateStrokeWidth() {
        subtitleTextView.updateStrokeWidth()
    }

    override fun updateTextColor() {
        subtitleTextView.updateTextColor()
    }

    override fun updateStrokeColor() {
        subtitleTextView.updateStrokeColor()
    }

    override fun onSubtitleTextOutput(subtitle: MixedSubtitle) {
        when (subtitle.type) {
            SubtitleType.TEXT -> {
                //显示文字字幕前，清空图片字幕
                if (!subtitleImageView.isEmptySubtitle()) {
                    subtitleImageView.setSubtitle(null)
                }
                subtitleTextView.setSubtitle(subtitle.text)
            }
            SubtitleType.BITMAP -> {
                //显示图片字幕前，清空文字字幕
                if (!subtitleTextView.isEmptySubtitle()) {
                    subtitleTextView.setSubtitle(null)
                }
                subtitleImageView.setSubtitle(subtitle.bitmaps)
            }
            else -> {

            }
        }
    }

    fun getViews(): Array<InterControllerView> {
        return subtitleViews
    }
}