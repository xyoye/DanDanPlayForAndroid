package com.xyoye.player.info

import com.xyoye.player_component.R

enum class SettingAction(val type: SettingActionType, val display: String, val icon: Int) {
    AUDIO_TRACK(SettingActionType.VIDEO, "音轨", R.drawable.ic_setting_audio_track),

    VIDEO_ASPECT(SettingActionType.VIDEO, "比例", R.drawable.ic_setting_video_aspect),

    VIDEO_SPEED(SettingActionType.VIDEO, "倍速", R.drawable.ic_setting_video_speed),

    BACKGROUND_PLAY(SettingActionType.VIDEO, "后台播放", R.drawable.ic_setting_background_play),

    DANMU_TRACK(SettingActionType.DANMU, "弹幕轨", R.drawable.ic_setting_danmu_track),

    DANMU_CONFIG(SettingActionType.DANMU, "配置", R.drawable.ic_setting_danmu_block),

    DANMU_STYLE(SettingActionType.DANMU, "样式", R.drawable.ic_setting_style),

    DANMU_TIME(SettingActionType.DANMU, "时间", R.drawable.ic_setting_time),

    SUBTITLE_TRACK(SettingActionType.SUBTITLE, "字幕轨", R.drawable.ic_setting_subtitle_track),

    SUBTITLE_STYLE(SettingActionType.SUBTITLE, "样式", R.drawable.ic_setting_style),

    SUBTITLE_TIME(SettingActionType.SUBTITLE, "时间", R.drawable.ic_setting_time),

    SCREEN_ORIENTATION(SettingActionType.OTHER, "屏幕翻转", R.drawable.ic_setting_rotate),

    NEXT_EPISODE(SettingActionType.OTHER, "顺序播放", R.drawable.ic_setting_order_play),

    SCREEN_SHOT(SettingActionType.OTHER, "截屏", R.mipmap.ic_video_screenshot),
}