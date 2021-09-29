package com.xyoye.stream_component.ui.activities.remote_control

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.data_component.data.remote.RemotePlayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class RemoteControlViewModel : BaseViewModel() {

    @Volatile
    private var playInfo: RemotePlayInfo? = null

    val volume = ObservableField<String>()
    val videoTitle = ObservableField<String>()
    val episodeTitle = ObservableField<String>()

    val duration = ObservableField<String>()
    val progress = ObservableField<String>()
    val position = ObservableField<Int>()

    val isPlaying = ObservableField<Boolean>()

    val updatePlayState = MutableLiveData<Boolean>()
    val vibrateLiveData = MutableLiveData<Boolean>()

    /**
     * 获取当前播放的视频信息
     */
    fun getPlayInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            //当前页面未退出前，死循环获取当前正在播放的视频信息
            while (true) {
                try {
                    val playInfo = Retrofit.remoteService.getPlayInfo()
                    updatePlayInfo(playInfo)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
                //每次间隔1s
                delay(1000L)
            }
        }
    }

    /**
     * 音量+
     */
    fun volumeAdd() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return
        var volume = playInfo!!.Volume + 1
        volume = min(volume, 100)

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.volume(volume.toString())
            }
        }
    }

    /**
     * 音量-
     */
    fun volumeReduce() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return
        var volume = playInfo!!.Volume - 1
        volume = max(volume, 0)

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.volume(volume.toString())
            }
        }
    }

    /**
     * 前进10s
     */
    fun fastForward() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return
        var progress = (playInfo!!.Duration * playInfo!!.Position).toLong()
        progress = min(playInfo!!.Duration, progress + (10 * 1000))

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.seek(progress.toString())
            }
        }
    }

    /**
     * 后退10s
     */
    fun fastBack() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return
        var progress = (playInfo!!.Duration * playInfo!!.Position).toLong()
        progress = max(0, progress - (10 * 1000))

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.seek(progress.toString())
            }
        }
    }

    /**
     * 下一视频
     */
    fun nextVideo() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.control("next")
            }
        }
    }

    /**
     * 上一视频
     */
    fun previousVideo() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return

        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.control("previous")
            }
        }
    }

    fun toggleStatus() {
        vibrateLiveData.postValue(true)
        if (playInfo == null)
            return

        val isPlaying = isPlaying.get() ?: false
        val method = if (isPlaying) "pause" else "play"
        httpRequest<Any>(viewModelScope) {
            api {
                Retrofit.remoteService.control(method)
            }
        }
    }

    /**
     * 更新界面信息
     */
    private fun updatePlayInfo(playInfo: RemotePlayInfo) {
        this.playInfo = playInfo

        playInfo.apply {
            volume.set(Volume.toString())
            videoTitle.set(AnimeTitle)
            episodeTitle.set(EpisodeTitle)

            duration.set(formatDuration(Duration))

            val progressValue = (Duration * Position).toLong()
            progress.set(formatDuration(progressValue))

            val positionValue = (Position * 100).toInt()
            position.set(positionValue)

            isPlaying.set(Playing)
        }

        updatePlayState.postValue(true)
    }
}