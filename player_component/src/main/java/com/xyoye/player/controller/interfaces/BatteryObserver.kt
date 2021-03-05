package com.xyoye.player.controller.interfaces

/**
 * Created by xyoye on 2020/11/3.
 */

interface BatteryObserver {
    fun onBatteryChange(percent: Int)
}