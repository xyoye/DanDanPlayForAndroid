package com.xyoye.common_component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by xyoye on 2020/2/18.
 */

public class HeadsetBroadcastReceiver extends BroadcastReceiver {

    private final PlayerReceiverListener listener;

    public HeadsetBroadcastReceiver(PlayerReceiverListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            listener.onHeadsetRemoved();
        }
    }
}
