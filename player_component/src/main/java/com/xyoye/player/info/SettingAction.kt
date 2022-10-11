package com.xyoye.player.info

enum class SettingAction(val type: SettingActionType, val display: String, val icon: Int) {
    VIDEO_ASPECT(SettingActionType.VIDEO, "比例", 0),

    VIDEO_SPEED(SettingActionType.VIDEO, "倍速", 0),

    DANMU_LOAD(SettingActionType.DANMU, "装载", 0),

    DANMU_STYLE(SettingActionType.DANMU, "样式", 0),

    DANMU_CONFIG(SettingActionType.DANMU, "配置", 0),

    DANMU_TIME(SettingActionType.DANMU, "时间", 0),

    SUBTITLE_LOAD(SettingActionType.SUBTITLE, "装载", 0),

    SUBTITLE_STREAM(SettingActionType.SUBTITLE, "字幕轨", 0),

    SUBTITLE_STYLE(SettingActionType.SUBTITLE, "样式", 0),

    SUBTITLE_TIME(SettingActionType.SUBTITLE, "时间", 0),

    VIDEO_STREAM(SettingActionType.OTHER, "音轨", 0),

    SCREEN_ORIENTATION(SettingActionType.OTHER, "屏幕翻转", 0),

    NEXT_EPISODE(SettingActionType.OTHER, "自动换集", 0),
}