package com.xyoye.player.controller.setting

import android.content.Context
import android.view.KeyEvent
import androidx.lifecycle.LiveData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.InterSettingController

/**
 * Created by xyoye on 2021/4/14.
 */

class SettingController(
    private val context: Context,
    private val addView: (InterSettingView) -> Unit
) : InterSettingController {

    private lateinit var playerSettingView: PlayerSettingView
    private lateinit var switchSourceView: SwitchSourceView
    private lateinit var switchVideoSourceView: SwitchVideoSourceView
    private lateinit var keywordBlockView: KeywordBlockView
    private lateinit var screenShotView: ScreenShotView
    private lateinit var settingDanmuStyleView: SettingDanmuStyleView
    private lateinit var searchDanmuView: SearchDanmuView
    private lateinit var videoSpeedView: SettingVideoSpeedView
    private lateinit var videoAspectView: SettingVideoAspectView
    private lateinit var audioStreamView: SettingAudioStreamView
    private lateinit var danmuConfigureView: SettingDanmuConfigureView
    private lateinit var offsetTimeView: SettingOffsetTimeView
    private lateinit var subtitleStreamView: SettingSubtitleStreamView
    private lateinit var subtitleStyleView: SettingSubtitleStyleView

    private val showingSettingViews = mutableListOf<InterSettingView>()
    private var isPopupMode = false

    override fun isSettingViewShowing(): Boolean {
        return showingSettingViews.find { it.isSettingShowing() } != null
    }

    override fun showSettingView(viewType: SettingViewType) {
        if (isPopupMode) {
            return
        }

        val settingView = getSettingView(viewType)
        if (settingView.isSettingShowing().not()) {
            showingSettingViews.add(settingView)
            settingView.onSettingVisibilityChanged(true)
        }
    }

    override fun hideSettingView() {
        val iterator = showingSettingViews.iterator()
        while (iterator.hasNext()) {
            val view = iterator.next()
            if (view.isSettingShowing()) {
                view.onSettingVisibilityChanged(false)
                iterator.remove()
            }
        }
    }

    override fun onDanmuSourceChanged() {

    }

    override fun onSubtitleSourceChanged() {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val iterator = showingSettingViews.iterator()
        while (iterator.hasNext()) {
            val view = iterator.next()
            if (view.isSettingShowing()) {
                if (view.onKeyDown(keyCode, event)) {
                    return true
                }
            }
        }
        return false
    }

    override fun settingRelease() {

    }

    fun setDatabaseBlock(
        add: ((keyword: String, isRegex: Boolean) -> Unit),
        remove: ((id: Int) -> Unit),
        queryAll: () -> LiveData<MutableList<DanmuBlockEntity>>
    ) {
        (getSettingView(SettingViewType.KEYWORD_BLOCK) as KeywordBlockView)
            .setDatabaseBlock(add, remove, queryAll)
    }

    fun setDanmuSearch(
        search: (String) -> Unit,
        download: (DanmuEpisodeData) -> Unit,
        searchResult: () -> LiveData<List<DanmuEpisodeData>>
    ) {
        (getSettingView(SettingViewType.SEARCH_DANMU) as SearchDanmuView)
            .setDanmuSearch(search, download, searchResult)
    }

    fun setSwitchVideoSourceBlock(block: (Int) -> Unit) {
        (getSettingView(SettingViewType.SWITCH_VIDEO_SOURCE) as SwitchVideoSourceView)
            .setSwitchVideoSourceBlock(block)
    }

    private fun getSettingView(type: SettingViewType): InterSettingView {
        when (type) {
            SettingViewType.PLAYER_SETTING -> {
                if (this::playerSettingView.isInitialized.not()) {
                    playerSettingView = PlayerSettingView(context)
                    addView.invoke(playerSettingView)
                }
                return playerSettingView
            }

            SettingViewType.SWITCH_SOURCE -> {
                if (this::switchSourceView.isInitialized.not()) {
                    switchSourceView = SwitchSourceView(context)
                    addView.invoke(switchSourceView)
                }
                return switchSourceView
            }

            SettingViewType.SWITCH_VIDEO_SOURCE -> {
                if (this::switchVideoSourceView.isInitialized.not()) {
                    switchVideoSourceView = SwitchVideoSourceView(context)
                    addView.invoke(switchVideoSourceView)
                }
                return switchVideoSourceView
            }

            SettingViewType.KEYWORD_BLOCK -> {
                if (this::keywordBlockView.isInitialized.not()) {
                    keywordBlockView = KeywordBlockView(context)
                    addView.invoke(keywordBlockView)
                }
                return keywordBlockView
            }

            SettingViewType.SCREEN_SHOT -> {
                if (this::screenShotView.isInitialized.not()) {
                    screenShotView = ScreenShotView(context)
                    addView.invoke(screenShotView)
                }
                return screenShotView
            }

            SettingViewType.SEARCH_DANMU -> {
                if (this::searchDanmuView.isInitialized.not()) {
                    searchDanmuView = SearchDanmuView(context)
                    addView.invoke(searchDanmuView)
                }
                return searchDanmuView
            }

            SettingViewType.VIDEO_SPEED -> {
                if (this::videoSpeedView.isInitialized.not()) {
                    videoSpeedView = SettingVideoSpeedView(context)
                    addView.invoke(videoSpeedView)
                }
                return videoSpeedView
            }

            SettingViewType.VIDEO_ASPECT -> {
                if (this::videoAspectView.isInitialized.not()) {
                    videoAspectView = SettingVideoAspectView(context)
                    addView.invoke(videoAspectView)
                }
                return videoAspectView
            }

            SettingViewType.AUDIO_STREAM -> {
                if (this::audioStreamView.isInitialized.not()) {
                    audioStreamView = SettingAudioStreamView(context)
                    addView.invoke(audioStreamView)
                }
                return audioStreamView
            }

            SettingViewType.LOAD_DANMU_SOURCE,
            SettingViewType.LOAD_SUBTITLE_SOURCE,
            SettingViewType.LOAD_AUDIO_SOURCE -> {
                if (this::switchSourceView.isInitialized.not()) {
                    switchSourceView = SwitchSourceView(context)
                    addView.invoke(switchSourceView)
                }
                switchSourceView.setSwitchType(type)
                return switchSourceView
            }

            SettingViewType.DANMU_STYLE -> {
                if (this::settingDanmuStyleView.isInitialized.not()) {
                    settingDanmuStyleView = SettingDanmuStyleView(context)
                    addView.invoke(settingDanmuStyleView)
                }
                return settingDanmuStyleView
            }

            SettingViewType.DANMU_CONFIGURE -> {
                if (this::danmuConfigureView.isInitialized.not()) {
                    danmuConfigureView = SettingDanmuConfigureView(context)
                    addView.invoke(danmuConfigureView)
                }
                return danmuConfigureView
            }

            SettingViewType.DANMU_OFFSET_TIME, SettingViewType.SUBTITLE_OFFSET_TIME -> {
                if (this::offsetTimeView.isInitialized.not()) {
                    offsetTimeView = SettingOffsetTimeView(context)
                    addView.invoke(offsetTimeView)
                }
                offsetTimeView.setSettingType(type)
                return offsetTimeView
            }

            SettingViewType.SUBTITLE_STREAM -> {
                if (this::subtitleStreamView.isInitialized.not()) {
                    subtitleStreamView = SettingSubtitleStreamView(context)
                    addView.invoke(subtitleStreamView)
                }
                return subtitleStreamView
            }

            SettingViewType.SUBTITLE_STYLE -> {
                if (this::subtitleStyleView.isInitialized.not()) {
                    subtitleStyleView = SettingSubtitleStyleView(context)
                    addView.invoke(subtitleStyleView)
                }
                return subtitleStyleView
            }
        }
    }

    fun setPopupMode(isPopupMode: Boolean) {
        this.isPopupMode = isPopupMode
        if (isPopupMode) {
            hideSettingView()
        }
    }
}