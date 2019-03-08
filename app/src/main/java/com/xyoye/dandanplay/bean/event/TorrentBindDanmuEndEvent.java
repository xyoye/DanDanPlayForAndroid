package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2019/3/5.
 */

public class TorrentBindDanmuEndEvent {
    private int episodeId;
    private String danmuPath;
    private int position;

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
