package com.xyoye.player.controller.setting

import android.content.Context
import android.view.KeyEvent
import androidx.lifecycle.LiveData
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.LoadDanmuState
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
    private lateinit var danmuSettingView: SettingDanmuView
    private lateinit var subtitleSettingView: SettingSubtitleView
    private lateinit var switchSourceView: SwitchSourceView
    private lateinit var switchVideoSourceView: SwitchVideoSourceView
    private lateinit var keywordBlockView: KeywordBlockView
    private lateinit var screenShotView: ScreenShotView
    private lateinit var settingDanmuConfigView: SettingDanmuConfigView
    private lateinit var settingDanmuBlockView: SettingDanmuBlockView
    private lateinit var searchDanmuView: SearchDanmuView
    private lateinit var videoSpeedView: SettingVideoSpeedView

    private val showingSettingViews = mutableListOf<InterSettingView>()

    override fun switchSource(isSwitchSubtitle: Boolean) {
        (getSettingView(SettingViewType.SWITCH_SOURCE) as SwitchSourceView)
            .setSwitchType(isSwitchSubtitle)
    }

    override fun isSettingViewShowing(): Boolean {
        return showingSettingViews.find { it.isSettingShowing() } != null
    }

    override fun showSettingView(viewType: SettingViewType) {
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
        (getSettingView(SettingViewType.DANMU_SETTING) as SettingDanmuView)
            .onDanmuSourceChanged()
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
        download: (DanmuSourceContentBean) -> Unit,
        searchResult: () -> LiveData<List<DanmuSourceContentBean>>
    ) {
        (getSettingView(SettingViewType.SEARCH_DANMU) as SearchDanmuView)
            .setDanmuSearch(search, download, searchResult)
    }

    fun setSwitchVideoSourceBlock(block: (Int) -> Unit) {
        (getSettingView(SettingViewType.SWITCH_VIDEO_SOURCE) as SwitchVideoSourceView)
            .setSwitchVideoSourceBlock(block)
    }

    fun updateTrack(isAudio: Boolean, trackData: MutableList<VideoTrackBean>) {
        if (isAudio) {
//            (getSettingView(SettingViewType.PLAYER_SETTING) as SettingPlayerView)
//                .updateAudioTrack(trackData)
        } else {
            (getSettingView(SettingViewType.SUBTITLE_SETTING) as SettingSubtitleView)
                .updateSubtitleTrack(trackData)
        }
    }

    fun updateLoadDanmuState(state: LoadDanmuState) {
        (getSettingView(SettingViewType.DANMU_SETTING) as SettingDanmuView)
            .updateLoadDanmuSate(state)
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
            SettingViewType.DANMU_SETTING -> {
                if (this::danmuSettingView.isInitialized.not()) {
                    danmuSettingView = SettingDanmuView(context)
                    addView.invoke(danmuSettingView)
                }
                return danmuSettingView
            }
            SettingViewType.SUBTITLE_SETTING -> {
                if (this::subtitleSettingView.isInitialized.not()) {
                    subtitleSettingView = SettingSubtitleView(context)
                    addView.invoke(subtitleSettingView)
                }
                return subtitleSettingView
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
            SettingViewType.DANMU_SETTING_CONFIG -> {
                if (this::settingDanmuConfigView.isInitialized.not()) {
                    settingDanmuConfigView = SettingDanmuConfigView(context)
                    addView.invoke(settingDanmuConfigView)
                }
                return settingDanmuConfigView
            }
            SettingViewType.DANMU_SETTING_BLOCK -> {
                if (this::settingDanmuBlockView.isInitialized.not()) {
                    settingDanmuBlockView = SettingDanmuBlockView(context)
                    addView.invoke(settingDanmuBlockView)
                }
                return settingDanmuBlockView
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
        }
    }
}