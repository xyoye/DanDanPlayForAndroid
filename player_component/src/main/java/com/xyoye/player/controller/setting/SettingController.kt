package com.xyoye.player.controller.setting

import android.content.Context
import androidx.lifecycle.LiveData
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.InterSettingController

/**
 * Created by xyoye on 2021/4/14.
 */

class SettingController(context: Context) : InterSettingController {

    private val playerSettingView = SettingPlayerView(context)
    private val danmuSettingView = SettingDanmuView(context)
    private val subtitleSettingView = SettingSubtitleView(context)
    private val switchSourceView = SwitchSourceView(context)
    private val switchVideoSourceView = SwitchVideoSourceView(context)
    private val keywordBlockView = KeywordBlockView(context)
    private val screenShotView = ScreenShotView(context)
    private val settingDanmuConfigView = SettingDanmuConfigView(context)
    private val settingDanmuBlockView = SettingDanmuBlockView(context)

    private val settingViews : Array<InterSettingView> = arrayOf(
        playerSettingView,
        danmuSettingView,
        subtitleSettingView,
        switchSourceView,
        switchVideoSourceView,
        keywordBlockView,
        screenShotView,
        settingDanmuConfigView,
        settingDanmuBlockView
    )

    override fun switchSource(isSwitchSubtitle: Boolean) {
        switchSourceView.setSwitchType(isSwitchSubtitle)
    }

    override fun isSettingViewShowing(): Boolean {
        return settingViews.find { it.isSettingShowing() } != null
    }

    override fun showSettingView(viewType: SettingViewType) {
        settingViews.forEach {
            if (it.getSettingViewType() == viewType && !it.isSettingShowing()){
                it.onSettingVisibilityChanged(true)
            }
        }
    }

    override fun hideSettingView() {
        settingViews.forEach {
            if (it.isSettingShowing()){
                it.onSettingVisibilityChanged(false)
            }
        }
    }

    override fun onDanmuSourceChanged() {
        danmuSettingView.onDanmuSourceChanged()
    }

    override fun onSubtitleSourceChanged() {

    }

    override fun settingRelease() {

    }

    fun getViews(): Array<InterSettingView> {
        return settingViews
    }

    fun setDatabaseBlock(
        add: ((keyword: String, isRegex: Boolean) -> Unit),
        remove: ((id: Int) -> Unit),
        queryAll: () -> LiveData<MutableList<DanmuBlockEntity>>
    ) {
        keywordBlockView.setDatabaseBlock(add, remove, queryAll)
    }

    fun setSwitchVideoSourceBlock(block: (Int) -> Unit) {
        switchVideoSourceView.setSwitchVideoSourceBlock(block)
    }

    fun updateTrack(isAudio: Boolean, trackData: MutableList<VideoTrackBean>) {
        if (isAudio){
            playerSettingView.updateAudioTrack(trackData)
        } else {
            subtitleSettingView.updateSubtitleTrack(trackData)
        }
    }
}