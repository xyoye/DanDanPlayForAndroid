package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.net.CommJsonEntity;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.net.RetroFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/7/20.
 */


public class AnimeDetailBean extends CommJsonEntity implements Serializable {

    /**
     * bangumi : {"episodes":[{"episodeId":0,"episodeTitle":"string","lastWatched":"2018-07-20T08:52:03.745Z","airDate":"2018-07-20T08:52:03.745Z"}],"summary":"string","bangumiUrl":"string","rating":0,"userRating":0,"favoriteStatus":"favorited","ratingDetails":{},"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true}
     */

    private BangumiBean bangumi;

    public BangumiBean getBangumi() {
        return bangumi;
    }

    public void setBangumi(BangumiBean bangumi) {
        this.bangumi = bangumi;
    }

    public static class BangumiBean implements Serializable {
        /**
         * episodes : [{"episodeId":0,"episodeTitle":"string","lastWatched":"2018-07-20T08:52:03.745Z","airDate":"2018-07-20T08:52:03.745Z"}]
         * summary : string
         * bangumiUrl : string
         * rating : 0
         * userRating : 0
         * favoriteStatus : favorited
         * ratingDetails : {}
         * animeId : 0
         * animeTitle : string
         * imageUrl : string
         * searchKeyword : string
         * isOnAir : true
         * airDay : 0
         * isFavorited : true
         * isRestricted : true
         */

        private String summary;
        private String bangumiUrl;
        private int rating;
        private int userRating;
        private String favoriteStatus;
        private RatingDetailsBean ratingDetails;
        private int animeId;
        private String animeTitle;
        private String imageUrl;
        private String searchKeyword;
        private boolean isOnAir;
        private int airDay;
        private boolean isFavorited;
        private boolean isRestricted;
        private List<EpisodesBean> episodes;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getBangumiUrl() {
            return bangumiUrl;
        }

        public void setBangumiUrl(String bangumiUrl) {
            this.bangumiUrl = bangumiUrl;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getUserRating() {
            return userRating;
        }

        public void setUserRating(int userRating) {
            this.userRating = userRating;
        }

        public String getFavoriteStatus() {
            return favoriteStatus;
        }

        public void setFavoriteStatus(String favoriteStatus) {
            this.favoriteStatus = favoriteStatus;
        }

        public RatingDetailsBean getRatingDetails() {
            return ratingDetails;
        }

        public void setRatingDetails(RatingDetailsBean ratingDetails) {
            this.ratingDetails = ratingDetails;
        }

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

        public List<EpisodesBean> getEpisodes() {
            return episodes;
        }

        public void setEpisodes(List<EpisodesBean> episodes) {
            this.episodes = episodes;
        }

        public static class RatingDetailsBean implements Serializable {
        }

        public static class EpisodesBean implements Serializable {
            /**
             * episodeId : 0
             * episodeTitle : string
             * lastWatched : 2018-07-20T08:52:03.745Z
             * airDate : 2018-07-20T08:52:03.745Z
             */

            private int episodeId;
            private String episodeTitle;
            private String lastWatched;
            private String airDate;

            public int getEpisodeId() {
                return episodeId;
            }

            public void setEpisodeId(int episodeId) {
                this.episodeId = episodeId;
            }

            public String getEpisodeTitle() {
                return episodeTitle;
            }

            public void setEpisodeTitle(String episodeTitle) {
                this.episodeTitle = episodeTitle;
            }

            public String getLastWatched() {
                return lastWatched;
            }

            public void setLastWatched(String lastWatched) {
                this.lastWatched = lastWatched;
            }

            public String getAirDate() {
                return airDate;
            }

            public void setAirDate(String airDate) {
                this.airDate = airDate;
            }
        }
    }

    public static void getAnimaDetail(String animaId, CommJsonObserver<AnimeDetailBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getAnimaDetail(animaId)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void addFavorite(String animaId, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        Map<String, String> map = new HashMap<>();
        map.put("animeId", animaId);
        map.put("favoriteStatus", "favorited");
        map.put("rating", "0");
        RetroFactory.getInstance().addFavorite(map)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void reduceFavorite(String animaId, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().reduceFavorite(animaId)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
