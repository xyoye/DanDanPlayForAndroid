package com.xyoye.dandanplay.bean.event;

/**
 * Created by YE on 2018/11/4.
 */


public class VideoActionEvent {
    public final static int DELETE = 101;
    public final static int UN_BIND = 102;

    private int actionType;
    private int position;

    public VideoActionEvent(int actionType, int position) {
        this.actionType = actionType;
        this.position = position;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
