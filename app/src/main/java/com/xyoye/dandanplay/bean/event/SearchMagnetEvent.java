package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/10/15.
 */

public class SearchMagnetEvent {
    private int position;
    private String episodeName;
    private int episodeId;

    public SearchMagnetEvent(int position, String episodeName, int episodeId) {
        this.position = position;
        this.episodeName = episodeName;
        this.episodeId = episodeId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }
}
