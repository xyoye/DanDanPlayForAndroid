package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/12/12.
 */

public class PatchFixEvent {
    private int code;
    private String msg;
    private int version;
    private String time;

    public PatchFixEvent() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
