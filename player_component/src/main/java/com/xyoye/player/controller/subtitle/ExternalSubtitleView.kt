package com.xyoye.player.controller.subtitle

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.subtitle.ExternalSubtitleManager
import com.xyoye.subtitle.MixedSubtitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2023/3/23
 */

class ExternalSubtitleView(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterControllerView {
    private lateinit var mControlWrapper: ControlWrapper

    private val lifecycleScope = (context as AppCompatActivity).lifecycleScope

    // 外挂字幕管理器
    private val mSubtitleManager = ExternalSubtitleManager()

    // 寻找字幕的Job
    private var mFindSubtitleJob: Job? = null

    // 当前已添加的字幕轨道，不一定被成功加载或选中
    private var mAddedTrack: VideoTrackBean? = null

    // 当前字幕轨道是否被选中
    private var mTrackSelected = false

    // 字幕是否加载完成
    private var mSubtitleLoaded = false

    // 是否可以执行寻找字幕
    private val canFindSubtitle: Boolean get() = mSubtitleLoaded && mTrackSelected

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean) {
    }

    override fun onPlayStateChanged(playState: PlayState) {
        when (playState) {
            PlayState.STATE_IDLE -> {
                mSubtitleManager.release()
            }

            PlayState.STATE_COMPLETED, PlayState.STATE_ERROR, PlayState.STATE_PAUSED -> {
                sendEmptySubtitle()
            }

            else -> {
            }
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {
        findSubtitle(position)
    }

    private fun findSubtitle(position: Long) {
        if (canFindSubtitle.not()) {
            return
        }

        mFindSubtitleJob?.cancel()
        mFindSubtitleJob = lifecycleScope.launch(Dispatchers.IO) {
            val subtitleTime = position + PlayerInitializer.Subtitle.offsetPosition
            val subtitle = mSubtitleManager.getSubtitle(subtitleTime)
                ?: return@launch
            launch(Dispatchers.Main) {
                mControlWrapper.onSubtitleTextOutput(subtitle)
            }
        }
    }

    private fun sendEmptySubtitle() {
        mControlWrapper.onSubtitleTextOutput(MixedSubtitle.fromText(""))
    }

    fun addTrack(track: VideoTrackBean): Boolean {
        // 外挂字幕地址无效，视为添加失败
        val subtitlePath = track.type.getSubtitle(track.trackResource)
        if (subtitlePath.isNullOrEmpty()) {
            return false
        }

        // 当前已加载的字幕地址与新添加的字幕地址相同，视为添加失败
        val loadedTrack = mAddedTrack
        if (loadedTrack != null) {
            val loadedSubtitle = loadedTrack.type.getSubtitle(loadedTrack.trackResource)
            if (subtitlePath == loadedSubtitle) {
                return false
            }
        }

        // 记录轨道数据
        mAddedTrack = track
        // 异步加载字幕
        loadSubtitleAsync(subtitlePath)
        return true
    }

    fun getAddedTrack(): VideoTrackBean? {
        return mAddedTrack?.copy(selected = mTrackSelected)
    }

    fun setTrackSelected(selected: Boolean) {
        mTrackSelected = selected
    }

    fun updateOffsetTime() {
        // 当字幕偏移时间改变时，可以立即重新寻找字幕
        // 但由于当前是每500ms寻找一次字幕，所以忽略此操作
    }

    private fun loadSubtitleAsync(subtitlePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 设置加载状态未完成
            mSubtitleLoaded = false
            // 释放旧的已加载的弹幕数据
            mSubtitleManager.release()
            // 发送一条空字幕，用于清空上一条显示的字幕
            sendEmptySubtitle()

            // 加载字幕
            mSubtitleLoaded = mSubtitleManager.loadSubtitle(subtitlePath)
        }
    }
}