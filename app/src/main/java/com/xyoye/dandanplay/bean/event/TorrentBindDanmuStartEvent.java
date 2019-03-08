package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2019/3/7.
 */

public class TorrentBindDanmuStartEvent {
    private String path;
    private int position;

    public TorrentBindDanmuStartEvent(String path, int position) {
        this.path = path;
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
