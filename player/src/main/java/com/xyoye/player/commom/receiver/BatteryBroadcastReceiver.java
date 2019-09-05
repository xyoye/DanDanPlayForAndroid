package com.xyoye.player.commom.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * 电量改变广播
 *
 * Created by xyoye on 2019/5/7.
 */

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    // 电量改变中
    public static final int BATTERY_STATUS_SPA = 101;
    // 低电量
    public static final int BATTERY_STATUS_LOW = 102;
    // 正常电量
    public static final int BATTERY_STATUS_NOR = 103;
    // 低电量临界值
    private static final int BATTERY_LOW_LEVEL = 15;

    private PlayerReceiverListener listener;

    public BatteryBroadcastReceiver(PlayerReceiverListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;
        // 接收电量变化信息
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            int status = intent.getIntExtra("status", BatteryManager.BATTERY_HEALTH_UNKNOWN);

            // 电量百分比
            int curPower = level * 100 / scale;

            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                listener.onBatteryChanged(BATTERY_STATUS_SPA, curPower);
            } else if (curPower < BATTERY_LOW_LEVEL) {
                listener.onBatteryChanged(BATTERY_STATUS_LOW, curPower);
            } else {
                listener.onBatteryChanged(BATTERY_STATUS_NOR, curPower);
            }
        }
    }
}