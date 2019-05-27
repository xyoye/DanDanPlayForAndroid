package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/10/24.
 */

public class SearchDanmuEvent {
    private String anime;
    private String episode;

    public SearchDanmuEvent(String anime, String episode) {
        this.anime = anime;
        this.episode = episode;
    }

    public String getAnime() {
        return anime;
    }

    public void setAnime(String anime) {
        this.anime = anime;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }
}
