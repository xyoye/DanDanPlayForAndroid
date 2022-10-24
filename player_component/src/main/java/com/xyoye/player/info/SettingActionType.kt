package com.xyoye.player.info

enum class SettingActionType(val widget: Int, val display: String) {
    VIDEO(0, "视频"),

    DANMU(1, "弹幕"),

    SUBTITLE(2, "字幕"),

    OTHER(3, "其它")
}