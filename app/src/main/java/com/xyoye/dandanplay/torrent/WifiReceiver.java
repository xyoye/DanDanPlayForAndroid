package com.xyoye.dandanplay.torrent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by xyoye on 2019/8/22.
 *
 * WiFi状态监听广播
 */

public class WifiReceiver extends BroadcastReceiver {
    private WifiStatusListener statusListener;

    public WifiReceiver(WifiStatusListener statusListener){
        this.statusListener = statusListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && statusListener != null) {
                statusListener.onStatusChanged(networkInfo.isConnected());
            }
        }
    }

    public interface WifiStatusListener{
        void onStatusChanged(boolean isConnected);
    }
}
