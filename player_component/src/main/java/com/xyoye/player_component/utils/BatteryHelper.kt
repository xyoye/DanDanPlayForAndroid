package com.xyoye.player_component.utils

import android.animation.ValueAnimator
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.receiver.BatteryBroadcastReceiver
import com.xyoye.player_component.R
import com.xyoye.player_component.widgets.BatteryView

/**
 * Created by xyoye on 2021/11/16.
 */

class BatteryHelper(
    private val activity: AppCompatActivity
) : LifecycleEventObserver {

    private val batteryReceiver = BatteryBroadcastReceiver(
        battery = { onBatterChanged(it) },
        charging = { onChargingChange(it) }
    )
    private var batteryView: BatteryView? = null
    private var batteryAnimator: ValueAnimator? = null
    private var mBattery = 0

    private var batteryLowColor = R.color.text_red.toResColor()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            registerReceiver()
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            unregisterReceiver()
        }
    }

    private fun onBatterChanged(battery: Int) {
        mBattery = battery
        batteryView?.setProgress(battery)

        if (battery <= 10) {
            batteryView?.setBatteryColor(batteryLowColor, batteryLowColor, batteryLowColor)
        } else {
            batteryView?.resetBatteryColor()
        }
    }

    private fun onChargingChange(charging: Boolean) {
        updateBatteryAnimator(mBattery, charging)
        batteryView?.setChargingFraction(mBattery / 100f)
    }

    private fun updateBatteryAnimator(battery: Int, charging: Boolean) {
        if (batteryAnimator != null) {
            //断开充电, 停止动画
            if (batteryView == null || charging.not()) {
                batteryAnimator!!.pause()
                return
            }
            //充满, 停止动画
            if (battery == 100) {
                batteryView!!.setChargingFraction(1f)
                batteryAnimator!!.pause()
                return
            }

            //更新动画初始值
            batteryAnimator!!.apply {
                pause()
                setIntValues(battery, 100)
                resume()
            }
            return
        }

        if (batteryView == null || charging.not())
            return

        //启动充电中动画
        batteryAnimator = ValueAnimator.ofFloat(battery.toFloat(), 100f).apply {
            duration = 2000L
            repeatCount = -1
            addUpdateListener {
                val value = it.animatedValue as Float
                val percent = value / 100f
                batteryView?.setChargingFraction(percent)
            }
            start()
        }
    }

    private fun registerReceiver() {
        activity.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        activity.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        activity.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
    }

    private fun unregisterReceiver() {
        activity.unregisterReceiver(batteryReceiver)
    }

    fun bindBatteryView(batteryView: BatteryView) {
        if (this.batteryView == null) {
            //只执行一次
            batteryView.post {
                activity.lifecycle.addObserver(this)
            }
        }
        this.batteryView = batteryView
    }
}