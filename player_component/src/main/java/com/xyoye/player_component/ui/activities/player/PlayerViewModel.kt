package com.xyoye.player_component.ui.activities.player

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.TrackType
import kotlinx.coroutines.launch
import master.flame.danmaku.danmaku.model.BaseDanmaku
import java.math.BigDecimal

class PlayerViewModel : BaseViewModel() {

    val localDanmuBlockLiveData = DatabaseManager.instance.getDanmuBlockDao().getAll(false)
    val cloudDanmuBlockLiveData = DatabaseManager.instance.getDanmuBlockDao().getAll(true)

    fun storeTrackAdded(videoSource: BaseVideoSource, track: VideoTrackBean) {
        val uniqueKey = videoSource.getUniqueKey()
        val storageId = videoSource.getStorageId()
        val historyDao = DatabaseManager.instance.getPlayHistoryDao()

        viewModelScope.launch {
            when (track.type) {
                TrackType.AUDIO -> {
                    val audioPath = track.type.getAudio(track.trackResource)
                    if (audioPath != null && audioPath != videoSource.getAudioPath()) {
                        videoSource.setAudioPath(audioPath)
                        historyDao.updateAudio(uniqueKey, storageId, audioPath)
                    }
                }

                TrackType.SUBTITLE -> {
                    val subtitlePath = track.type.getSubtitle(track.trackResource)
                    if (subtitlePath != null && subtitlePath != videoSource.getSubtitlePath()) {
                        videoSource.setSubtitlePath(subtitlePath)
                        historyDao.updateSubtitle(uniqueKey, storageId, subtitlePath)
                    }
                }

                TrackType.DANMU -> {
                    val danmu = track.type.getDanmu(track.trackResource)
                    if (danmu != null && danmu != videoSource.getDanmu()) {
                        videoSource.setDanmu(danmu)
                        historyDao.updateDanmu(uniqueKey, storageId, danmu.danmuPath, danmu.episodeId)
                    }
                }
            }
        }
    }

    fun addDanmuBlock(keyword: String, isRegex: Boolean) {
        viewModelScope.launch {
            DatabaseManager.instance.getDanmuBlockDao().insert(
                DanmuBlockEntity(0, keyword, isRegex)
            )
        }
    }

    fun removeDanmuBlock(id: Int) {
        viewModelScope.launch {
            DatabaseManager.instance.getDanmuBlockDao().delete(id)
        }
    }

    fun sendDanmu(danmu: LocalDanmuBean?, sendDanmuBean: SendDanmuBean) {
        val episodeId = danmu?.episodeId
        if (episodeId?.isNotEmpty() == true) {
            sendDanmuToServer(sendDanmuBean, episodeId)
        }

        val danmuPath = danmu?.danmuPath
        if (danmuPath?.isNotEmpty() == true) {
            writeDanmuToFile(sendDanmuBean, danmuPath)
        }
    }

    private fun sendDanmuToServer(sendDanmuBean: SendDanmuBean, episodeId: String) {
        viewModelScope.launch {
            val time = BigDecimal(sendDanmuBean.position.toDouble() / 1000)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString()

            val mode = when {
                sendDanmuBean.isScroll -> BaseDanmaku.TYPE_SCROLL_RL
                sendDanmuBean.isTop -> BaseDanmaku.TYPE_FIX_TOP
                else -> BaseDanmaku.TYPE_FIX_BOTTOM
            }

            val color = sendDanmuBean.color and 0x00FFFFFF

            val result = ResourceRepository.sendOneDanmu(
                episodeId,
                time,
                mode,
                color,
                sendDanmuBean.text
            )

            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message.orEmpty()
                ToastCenter.showOriginalToast("发送弹幕失败\n$message")
            }
        }
    }

    private fun writeDanmuToFile(sendDanmuBean: SendDanmuBean, danmuPath: String) {
        val time = BigDecimal(sendDanmuBean.position.toDouble() / 1000)
            .setScale(2, BigDecimal.ROUND_HALF_UP)
            .toString()

        val mode = when {
            sendDanmuBean.isScroll -> BaseDanmaku.TYPE_SCROLL_RL
            sendDanmuBean.isTop -> BaseDanmaku.TYPE_FIX_TOP
            else -> BaseDanmaku.TYPE_FIX_BOTTOM
        }

        val unixTime = (System.currentTimeMillis() / 1000f).toInt().toString()
        val color = sendDanmuBean.color and 0x00FFFFFF

        val danmuText = "<d p=\"$time,$mode,25,$color,$unixTime,0,0,0\">${sendDanmuBean.text}</d>"

        DanmuUtils.appendDanmu(danmuPath, danmuText)
    }
}