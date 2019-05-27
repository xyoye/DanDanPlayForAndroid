package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/11/20.
 */

public class OpenLanEvent {
    private int position;

    public OpenLanEvent( int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
