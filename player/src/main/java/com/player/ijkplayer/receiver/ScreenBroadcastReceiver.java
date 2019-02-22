package com.player.ijkplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 屏幕锁定广播
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {
    private PlayerReceiverListener listener;

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