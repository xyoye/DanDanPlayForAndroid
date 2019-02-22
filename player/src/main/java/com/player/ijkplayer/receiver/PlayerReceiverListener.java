package com.player.ijkplayer.receiver;

public interface PlayerReceiverListener{
    void onBatteryChanged(int status, int progress);

    void onScreenLocked();
}