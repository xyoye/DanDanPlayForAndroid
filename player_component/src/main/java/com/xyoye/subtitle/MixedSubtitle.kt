package com.xyoye.subtitle

import com.google.android.exoplayer2.text.Cue

/**
 * Created by xyoye on 2020/12/21.
 */

data class MixedSubtitle(
    val type: SubtitleType,

    val text: List<SubtitleText>?,

    val bitmaps: List<Cue>? = null
) {
    companion object {
        fun fromText(subtitleText: String?): MixedSubtitle {
            val text = SubtitleUtils.caption2Subtitle(subtitleText)
            return MixedSubtitle(SubtitleType.TEXT, text)
        }

        fun fromBitmap(subtitleBitmap: List<Cue>?): MixedSubtitle {
            return MixedSubtitle(SubtitleType.BITMAP, null, subtitleBitmap)
        }
    }
}