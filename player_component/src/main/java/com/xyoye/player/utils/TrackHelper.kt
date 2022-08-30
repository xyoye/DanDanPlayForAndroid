package com.xyoye.player.utils

import android.content.Context
import com.google.android.exoplayer2.C.TRACK_TYPE_AUDIO
import com.google.android.exoplayer2.C.TRACK_TYPE_TEXT
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.TrackNameProvider
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
        tracks: Tracks,
        trackNameProvider: TrackNameProvider
    ) {
        audioTrackData.clear()
        subtitleTrackData.clear()

        tracks.groups.forEachIndexed { groupIndex, group ->
            if (group.isSupported.not()) {
                return
            }
            if (group.type == TRACK_TYPE_AUDIO) {
                getExoGroupFormat(group).forEachIndexed { formatIndex, format ->
                    val track = VideoTrackBean(
                        trackName = trackNameProvider.getTrackName(format),
                        isAudio = true,
                        trackId = formatIndex,
                        isChecked = group.isSelected,
                        trackGroupId = groupIndex
                    )
                    audioTrackData.add(track)
                }
            } else if (group.type == TRACK_TYPE_TEXT) {
                getExoGroupFormat(group).forEachIndexed { formatIndex, format ->
                    val track = VideoTrackBean(
                        trackName = trackNameProvider.getTrackName(format),
                        isAudio = false,
                        trackId = formatIndex,
                        isChecked = group.isSelected,
                        trackGroupId = groupIndex
                    )
                    subtitleTrackData.add(track)
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

    fun selectExoTrack(
        context: Context,
        trackSelector: TrackSelector,
        track: VideoTrackBean?,
        tracks: Tracks
    ) {
        if (track == null) {
            trackSelector.parameters = TrackSelectionParameters.Builder(context)
                .clearOverridesOfType(TRACK_TYPE_TEXT)
                .setTrackTypeDisabled(TRACK_TYPE_TEXT, true)
                .build()
            return
        }

        val trackGroup = tracks.groups.getOrNull(track.trackGroupId)?.mediaTrackGroup
            ?: return
        val override = TrackSelectionOverride(trackGroup, track.trackId)

        val trackParams = TrackSelectionParameters.Builder(context)
            .setTrackTypeDisabled(TRACK_TYPE_TEXT, false)
            .clearOverridesOfType(trackGroup.type)
            .addOverride(override)
            .build()
        trackSelector.parameters = trackParams
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

    private fun getExoGroupFormat(group: Tracks.Group): List<Format> {
        val formats = mutableListOf<Format>()
        for (formatIndex in 0 until group.length) {
            val format = group.getTrackFormat(formatIndex)
            formats.add(format)
        }
        return formats
    }
}