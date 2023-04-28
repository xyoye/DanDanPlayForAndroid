package com.xyoye.common_component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 屏幕锁定广播
 *
 * Created by xyoye on 2019/5/7.
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {
    private final PlayerReceiverListener listener;

    public ScreenBroadcastReceiver(PlayerReceiverListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            listener.onScreenLocked();
        }
    }
}