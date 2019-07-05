package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2019/6/10.
 */

public class TorrentCheckBean {
    private String name;
    private long length;
    private boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
