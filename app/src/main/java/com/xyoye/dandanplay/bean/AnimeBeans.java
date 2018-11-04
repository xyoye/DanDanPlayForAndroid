package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/7/15.
 */


public class AnimeBeans extends CommJsonEntity implements Serializable {

    private List<BangumiListBean> bangumiList;

    public List<BangumiListBean> getBangumiList() {
        return bangumiList;
    }

    public void setBangumiList(List<BangumiListBean> bangumiList) {
        this.bangumiList = bangumiList;
    }

    public static class BangumiListBean implements Serializable {
        /**
         * animeId : 0
         * animeTitle : string
         * imageUrl : string
         * searchKeyword : string
         * isOnAir : true
         * airDay : 0
         * isFavorited : true
         * isRestricted : true
         * rating": 0
         */

        private int animeId;
        private String animeTitle;
        private String imageUrl;
        private String searchKeyword;
        private boolean isOnAir;
        private int airDay;
        private boolean isFavorited;
        private boolean isRestricted;
        private double rating;

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

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getSearchKeyword() {
            return searchKeyword;
        }

        public void setSearchKeyword(String searchKeyword) {
            this.searchKeyword = searchKeyword;
        }

        public boolean isIsOnAir() {
            return isOnAir;
        }

        public void setIsOnAir(boolean isOnAir) {
            this.isOnAir = isOnAir;
        }

        public int getAirDay() {
            return airDay;
        }

        public void setAirDay(int airDay) {
            this.airDay = airDay;
        }

        public boolean isIsFavorited() {
            return isFavorited;
        }

        public void setIsFavorited(boolean isFavorited) {
            this.isFavorited = isFavorited;
        }

        public boolean isIsRestricted() {
            return isRestricted;
        }

        public void setIsRestricted(boolean isRestricted) {
            this.isRestricted = isRestricted;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double String) {
            this.rating = rating;
        }
    }

    public static void getAnimes(CommJsonObserver<AnimeBeans> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getAnimes()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
