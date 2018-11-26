package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2018/11/20.
 */

public class UpdateDeviceEvent {
    private boolean delete;
    private int position;

    public UpdateDeviceEvent(boolean delete, int position) {
        this.delete = delete;
        this.position = position;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
