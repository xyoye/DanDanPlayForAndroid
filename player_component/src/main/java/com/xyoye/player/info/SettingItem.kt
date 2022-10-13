package com.xyoye.player.info

data class SettingItem(
    val action: SettingAction,
    val display: String = "",
    val icon: Int = 0,
    var selected: Boolean = false
)