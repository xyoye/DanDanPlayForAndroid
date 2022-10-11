package com.xyoye.player.info

data class SettingItem(
    val type: SettingActionType = SettingActionType.OTHER,
    val display: String = "",
    val icon: Int = 0,
    var selected: Boolean = false
)