package com.xyoye.player.controller.subtitle

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.data_component.bean.VideoStreamBean
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

    // 外挂字幕流
    private val externalSubtitleStream = mutableListOf<VideoStreamBean>()

    // 外挂字幕管理器
    private val mSubtitleManager = ExternalSubtitleManager()

    // 寻找字幕的Job
    private var mFindSubtitleJob: Job? = null

    private var externalTrackId = -2

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

    override fun onLockStateChanged(isLocked: Boolean) {
    }

    override fun onVideoSizeChanged(videoSize: Point) {
    }

    override fun onPopupModeChanged(isPopup: Boolean) {
    }

    private fun findSubtitle(position: Long) {
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

    fun addSubtitleStream(filePath: String) {
        val streamAdded = externalSubtitleStream.any { it.externalStreamPath == filePath }
        if (streamAdded) {
            return
        }

        val trackName = getFileNameNoExtension(filePath)
        val subtitleStream = VideoStreamBean(
            trackName = trackName,
            isAudio = false,
            trackId = externalTrackId,
            isExternalStream = true,
            externalStreamPath = filePath,
            isChecked = true
        )
        externalSubtitleStream.forEach { it.isChecked = false }
        externalSubtitleStream.add(subtitleStream)
        mControlWrapper.selectStream(subtitleStream)

        // 外挂字幕流ID向下减小
        externalTrackId--
    }

    fun selectSubtitleStream(stream: VideoStreamBean) {
        sendEmptySubtitle()
        mSubtitleManager.release()

        if (stream.isExternalStream.not()) {
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            mSubtitleManager.loadSubtitle(stream.externalStreamPath)
            mControlWrapper.onSubtitleSourceUpdate(stream.externalStreamPath)
        }
    }

    fun getExternalSubtitleStream(): List<VideoStreamBean> {
        return externalSubtitleStream
    }

    fun updateOffsetTime() {

    }
}