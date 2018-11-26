package com.xyoye.dandanplay.bean;

/**
 * Created by xyy on 2018/11/22.
 */

public class LanVideoBean {
    private String smbUrl;
    private String danmuPath;
    private String currentPosition;
    private String episodeId;

    public LanVideoBean() {
    }

    public LanVideoBean(String smbUrl, String danmuPath, String currentPosition, String episodeId) {
        this.smbUrl = smbUrl;
        this.danmuPath = danmuPath;
        this.currentPosition = currentPosition;
        this.episodeId = episodeId;
    }

    public String getSmbUrl() {
        return smbUrl;
    }

    public void setSmbUrl(String smbUrl) {
        this.smbUrl = smbUrl;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }
}
