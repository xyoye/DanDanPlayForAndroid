package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.net.CommJsonEntity;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.net.RetroFactory;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/7/24.
 */


public class AnimeFavoriteBean extends CommJsonEntity implements Serializable {

    private List<FavoritesBean> favorites;

    public List<FavoritesBean> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoritesBean> favorites) {
        this.favorites = favorites;
    }

    public static class FavoritesBean implements Serializable {
        /**
         * animeId : 0
         * animeTitle : string
         * lastFavoriteTime : 2018-07-24T06:00:23.633Z
         * imageUrl : string
         * episodeTotal : 0
         * episodeWatched : 0
         * isOnAir : true
         * favoriteStatus : favorited
         * userRating : 0
         */

        private int animeId;
        private String animeTitle;
        private String lastFavoriteTime;
        private String imageUrl;
        private int episodeTotal;
        private int episodeWatched;
        private boolean isOnAir;
        private String favoriteStatus;
        private int userRating;

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

        public String getLastFavoriteTime() {
            return lastFavoriteTime;
        }

        public void setLastFavoriteTime(String lastFavoriteTime) {
            this.lastFavoriteTime = lastFavoriteTime;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getEpisodeTotal() {
            return episodeTotal;
        }

        public void setEpisodeTotal(int episodeTotal) {
            this.episodeTotal = episodeTotal;
        }

        public int getEpisodeWatched() {
            return episodeWatched;
        }

        public void setEpisodeWatched(int episodeWatched) {
            this.episodeWatched = episodeWatched;
        }

        public boolean isIsOnAir() {
            return isOnAir;
        }

        public void setIsOnAir(boolean isOnAir) {
            this.isOnAir = isOnAir;
        }

        public String getFavoriteStatus() {
            return favoriteStatus;
        }

        public void setFavoriteStatus(String favoriteStatus) {
            this.favoriteStatus = favoriteStatus;
        }

        public int getUserRating() {
            return userRating;
        }

        public void setUserRating(int userRating) {
            this.userRating = userRating;
        }
    }

    public static void getFavorite(CommJsonObserver<AnimeFavoriteBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getFavorite()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
