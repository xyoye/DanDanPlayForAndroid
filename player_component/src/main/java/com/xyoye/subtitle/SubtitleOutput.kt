package com.xyoye.subtitle

/**
 * Created by xyoye on 2020/12/14.
 */

interface SubtitleOutput {
    fun onSubtitleOutput(subtitles: MutableList<SubtitleText>)

    fun getCurrentPosition(): Long
}