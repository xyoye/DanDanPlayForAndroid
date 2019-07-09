package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/7/9.
 */

public class AnimeTagBean extends CommJsonEntity {

    private List<AnimesBean> animes;

    public List<AnimesBean> getAnimes() {
        return animes;
    }

    public void setAnimes(List<AnimesBean> animes) {
        this.animes = animes;
    }

    public static class AnimesBean {
        /**
         * animeId : 0
         * animeTitle : string
         * type : tvseries
         * typeDescription : string
         * imageUrl : string
         * startDate : 2019-07-09T09:46:04.460Z
         * episodeCount : 0
         * rating : 0
         * isFavorited : true
         */

        private int animeId;
        private String animeTitle;
        private String type;
        private String typeDescription;
        private String imageUrl;
        private String startDate;
        private int episodeCount;
        private int rating;
        private boolean isFavorited;

        public int getAnimeId() {
            return animeId;
        }

        public void setAnimeId(int animeId) {
            this.animeId = animeId;
        }

        public String getAnimeTitle() {
            return animeTitle;
        }

        public void setAnimeTitle(String animeTitle) {
            this.animeTitle = animeTitle;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTypeDescription() {
            return typeDescription;
        }

        public void setTypeDescription(String typeDescription) {
            this.typeDescription = typeDescription;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public int getEpisodeCount() {
            return episodeCount;
        }

        public void setEpisodeCount(int episodeCount) {
            this.episodeCount = episodeCount;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public boolean isIsFavorited() {
            return isFavorited;
        }

        public void setIsFavorited(boolean isFavorited) {
            this.isFavorited = isFavorited;
        }
    }

    public static void getTagAnimeList(String tagId, CommJsonObserver<AnimeTagBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getAnimeListByTag(tagId)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
