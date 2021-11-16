package com.xyoye.common_component.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * Created by xyoye on 2021/11/16.
 */

class BatteryBroadcastReceiver(
    private val battery: (Int) -> Unit,
    private val charging: (Boolean) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val battery = level * 100 / scale
                this.battery.invoke(battery)

                val status = intent.getIntExtra(
                    BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN
                )
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    charging.invoke(true)
                }
            }
            Intent.ACTION_POWER_CONNECTED -> {
                charging.invoke(true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                charging.invoke(false)
            }
        }
    }
}