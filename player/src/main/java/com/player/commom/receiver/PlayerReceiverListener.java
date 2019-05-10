package com.player.commom.receiver;

public interface PlayerReceiverListener{
    void onBatteryChanged(int status, int progress);

    void onScreenLocked();
}