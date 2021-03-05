package com.xyoye.player.utils

import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.MimeTypes
import com.xyoye.data_component.bean.VideoTrackBean
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo


/**
 * Created by xyoye on 2020/11/16.
 */

object TrackHelper {

    val audioTrackData = MutableLiveData<MutableList<VideoTrackBean>>()
    val subtitleTrackData = MutableLiveData<MutableList<VideoTrackBean>>()

    fun initIjkTrack(
        trackInfo: Array<IjkTrackInfo>,
        audioId: Int,
        subtitleId: Int
    ) {
        val audioData = mutableListOf<VideoTrackBean>()
        val subtitleData = mutableListOf<VideoTrackBean>()

        for ((index, info) in trackInfo.withIndex()) {
            if (info.trackType == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                val trackName =
                    "#${audioData.size + 1}：${info.title}[${info.language}, ${info.codecName}]"
                audioData.add(VideoTrackBean(trackName, true, index, index == audioId))
            } else if (info.trackType == IjkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                subtitleData.add(VideoTrackBean(info.title, false, index, index == subtitleId))
            }
        }
        audioTrackData.postValue(audioData)
        subtitleTrackData.postValue(subtitleData)

    }

    fun initExoTrack(
        trackSelector: TrackSelector,
        trackSelections: TrackSelectionArray,
        trackNameProvider: TrackNameProvider
    ) {
        val audioData = mutableListOf<VideoTrackBean>()
        val subtitleData = mutableListOf<VideoTrackBean>()

        if (trackSelector !is MappingTrackSelector) {
            return
        }

        val trackInfo = trackSelector.currentMappedTrackInfo ?: return

        var selectedAudioId = ""
        var selectedSubtitleId = ""

        for (selection: TrackSelection? in trackSelections.all) {
            if (selection == null) continue
            if (MimeTypes.isAudio(selection.selectedFormat.sampleMimeType)) {
                selectedAudioId = selection.selectedFormat.id ?: ""
            } else if (MimeTypes.isText(selection.selectedFormat.sampleMimeType)) {
                selectedSubtitleId = selection.selectedFormat.id ?: ""
            }
        }

        for (groupArrayIndex in 0 until trackInfo.rendererCount) {
            val groupArray = trackInfo.getTrackGroups(groupArrayIndex)
            for (groupIndex in 0 until groupArray.length) {
                val group = groupArray.get(groupIndex)
                for (formatIndex in 0 until group.length) {
                    val format = group.getFormat(formatIndex)

                    val trackName = trackNameProvider.getTrackName(format)
                    val mineType = format.sampleMimeType ?: "und"

                    if (MimeTypes.isAudio(mineType)) {
                        val isChecked = selectedAudioId == format.id
                        audioData.add(
                            VideoTrackBean(
                                trackName,
                                true,
                                formatIndex,
                                isChecked,
                                groupArrayIndex,
                                groupIndex
                            )
                        )
                    } else if (MimeTypes.isText(mineType)) {
                        val isChecked = selectedSubtitleId == format.id
                        subtitleData.add(
                            VideoTrackBean(
                                trackName,
                                false,
                                formatIndex,
                                isChecked,
                                groupArrayIndex,
                                groupIndex
                            )
                        )
                    }
                }
            }
        }

        audioTrackData.postValue(audioData)
        subtitleTrackData.postValue(subtitleData)
    }

    fun selectExoTrack(trackSelector: TrackSelector, videoTrackBean: VideoTrackBean?) {
        if (trackSelector !is DefaultTrackSelector) {
            return
        }

        val trackInfo = trackSelector.currentMappedTrackInfo ?: return

        //只有字幕流才会被设置为空，设置为空时，关闭当前流
        if (videoTrackBean == null){
            for (renderIndex in 0 until trackInfo.rendererCount){
                if (trackInfo.getRendererType(renderIndex) == C.TRACK_TYPE_TEXT){
                    val parametersBuilder = trackSelector.parameters.buildUpon().apply {
                        setRendererDisabled(renderIndex, true)
                    }
                    trackSelector.setParameters(parametersBuilder)
                    break
                }
            }
            return
        }

        val trackGroupArray = trackInfo.getTrackGroups(videoTrackBean.renderId)
        val override = SelectionOverride(videoTrackBean.trackGroupId, videoTrackBean.trackId)
        val parametersBuilder = trackSelector.parameters.buildUpon().apply {
            setRendererDisabled(videoTrackBean.renderId, false)
            setSelectionOverride(videoTrackBean.renderId, trackGroupArray, override)
        }

        trackSelector.setParameters(parametersBuilder)
    }
}