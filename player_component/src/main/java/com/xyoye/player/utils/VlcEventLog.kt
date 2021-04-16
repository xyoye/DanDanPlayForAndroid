package com.xyoye.player.utils

import com.xyoye.common_component.utils.DDLog
import java.util.*

/**
 * Created by xyoye on 2021/4/16.
 */

object VlcEventLog {
    private val eventType = HashMap<Int, String>()

    init {
        eventType[0x100] = "MediaChanged"
        eventType[0x102] = "Opening"
        eventType[0x103] = "Buffering"
        eventType[0x104] = "Playing"
        eventType[0x105] = "Paused"
        eventType[0x106] = "Stopped"
        eventType[0x109] = "EndReached"
        eventType[0x10a] = "EncounteredError"
        eventType[0x10b] = "TimeChanged"
        eventType[0x10c] = "PositionChanged"
        eventType[0x10d] = "SeekableChanged"
        eventType[0x10e] = "PausableChanged"
        eventType[0x111] = "LengthChanged"
        eventType[0x112] = "Vout"
        eventType[0x114] = "ESAdded"
        eventType[0x115] = "ESDeleted"
        eventType[0x116] = "ESSelected"
    }

    fun log(type: Int){
        DDLog.i("VLC EVENT", eventType[type] ?: "Unknown")
    }
}