package com.xyoye.dandanplay.bean.params;

import java.util.List;

/**
 * Created by xyy on 2019/1/3.
 */

public class HistoryParam {


    /**
     * episodeIdList : [0]
     * addToFavorite : true
     * rating : 0
     */

    private boolean addToFavorite;
    private int rating;
    private List<Integer> episodeIdList;

    public boolean isAddToFavorite() {
        return addToFavorite;
    }

    public void setAddToFavorite(boolean addToFavorite) {
        this.addToFavorite = addToFavorite;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<Integer> getEpisodeIdList() {
        return episodeIdList;
    }

    public void setEpisodeIdList(List<Integer> episodeIdList) {
        this.episodeIdList = episodeIdList;
    }
}
