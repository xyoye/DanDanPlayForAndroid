package com.xyoye.player.utils

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.MimeTypes
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.player.kernel.inter.VideoPlayerEventListener
import org.videolan.libvlc.MediaPlayer
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo


/**
 * Created by xyoye on 2020/11/16.
 */

class TrackHelper(private val mPlayerEventListener: VideoPlayerEventListener) {
    private val audioTrackData = mutableListOf<VideoTrackBean>()
    private val subtitleTrackData = mutableListOf<VideoTrackBean>()

    fun initIjkTrack(
        trackInfo: Array<IjkTrackInfo>,
        audioId: Int,
        subtitleId: Int
    ) {
        audioTrackData.clear()
        subtitleTrackData.clear()

        for ((index, info) in trackInfo.withIndex()) {
            if (info.trackType == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                val trackName =
                    "#${audioTrackData.size + 1}：${info.title}[${info.language}, ${info.codecName}]"
                audioTrackData.add(VideoTrackBean(trackName, true, index, index == audioId))
            } else if (info.trackType == IjkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                subtitleTrackData.add(VideoTrackBean(info.title, false, index, index == subtitleId))
            }
        }
        mPlayerEventListener.updateTrack(true, audioTrackData)
        mPlayerEventListener.updateTrack(false, subtitleTrackData)
    }

    fun initExoTrack(
        trackSelector: TrackSelector,
        trackSelections: TrackSelectionArray,
        trackNameProvider: TrackNameProvider
    ) {
        if (trackSelector !is MappingTrackSelector) {
            return
        }

        val trackInfo = trackSelector.currentMappedTrackInfo ?: return

        var selectedAudioId = ""
        var selectedSubtitleId = ""

        // TODO: 2021/9/26 Track相关API已变更
//        for (selection: TrackSelection? in trackSelections.all) {
//            if (selection == null) continue
//            if (MimeTypes.isAudio(selection.selectedFormat.sampleMimeType)) {
//                selectedAudioId = selection.selectedFormat.id ?: ""
//            } else if (MimeTypes.isText(selection.selectedFormat.sampleMimeType)) {
//                selectedSubtitleId = selection.selectedFormat.id ?: ""
//            }
//        }

        audioTrackData.clear()
        subtitleTrackData.clear()
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
                        audioTrackData.add(
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
                        subtitleTrackData.add(
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

        mPlayerEventListener.updateTrack(true, audioTrackData)
        mPlayerEventListener.updateTrack(false, subtitleTrackData)
    }

    fun initVLCTrack(
        audioTracks: Array<MediaPlayer.TrackDescription>?,
        subtitleTracks: Array<MediaPlayer.TrackDescription>?
    ) {
        audioTrackData.clear()
        subtitleTrackData.clear()
        audioTracks?.forEach {
            audioTrackData.add(VideoTrackBean(it.name, true, it.id, false))
        }

        subtitleTracks?.forEach {
            subtitleTrackData.add(VideoTrackBean(it.name, false, it.id, false))
        }

        mPlayerEventListener.updateTrack(true, audioTrackData)
        mPlayerEventListener.updateTrack(false, subtitleTrackData)
    }

    fun selectExoTrack(trackSelector: TrackSelector, videoTrackBean: VideoTrackBean?) {
        if (trackSelector !is DefaultTrackSelector) {
            return
        }

        val trackInfo = trackSelector.currentMappedTrackInfo ?: return

        //只有字幕流才会被设置为空，设置为空时，关闭当前流
        if (videoTrackBean == null) {
            for (renderIndex in 0 until trackInfo.rendererCount) {
                if (trackInfo.getRendererType(renderIndex) == C.TRACK_TYPE_TEXT) {
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

    fun selectVLCTrack(isAudio: Boolean, trackId: Int) {
        if (isAudio) {
            audioTrackData.forEach { it.isChecked = it.trackId == trackId }
            mPlayerEventListener.updateTrack(true, audioTrackData)
        } else {
            subtitleTrackData.forEach { it.isChecked = it.trackId == trackId }
            mPlayerEventListener.updateTrack(false, subtitleTrackData)
        }
    }
}