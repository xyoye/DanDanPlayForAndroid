package com.xyoye.common_component.receiver;

/**
 * Created by xyoye on 2019/5/7.
 */
public interface PlayerReceiverListener{
    void onBatteryChanged(int status, int progress);

    void onScreenLocked();

    void onHeadsetRemoved();
}