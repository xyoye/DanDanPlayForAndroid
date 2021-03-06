package com.xyoye.subtitle

import com.google.android.exoplayer2.text.Cue

/**
 * Created by xyoye on 2020/12/21.
 */

data class MixedSubtitle(
    val type: SubtitleType,

    val text: String?,

    val bitmaps: List<Cue>? = null
)