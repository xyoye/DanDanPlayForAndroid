package com.xyoye.player.danmaku.controller;

import android.os.SystemClock;

/**
 * Created by xyoye on 2020/4/1.
 *
 * 区别于现实时间，用于弹幕倍速
 */

public class DimensionTimer {
    private long startTime;
    private float timeRate;

    private static class Holder {
        static DimensionTimer instance = new DimensionTimer();
    }

    private DimensionTimer() {
        startTime = SystemClock.uptimeMillis();
        timeRate = 1f;
    }

    public static DimensionTimer getInstance() {
        return Holder.instance;
    }

    public void init(){

    }

    public void setTimeRate(float timeRate) {
        this.timeRate = timeRate;
    }

    public long get2dTime() {
        long nowTime = SystemClock.uptimeMillis();
        long interval = nowTime - startTime;
        long interval2d = (long) (interval * timeRate);

        return startTime + interval2d;
    }
}
