package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/20.
 */


public class AnimeDetailBean extends CommJsonEntity implements Serializable {


    /**
     * bangumi : {"type":"tvseries","typeDescription":"string","episodes":[{"episodeId":0,"episodeTitle":"string","lastWatched":"2019-01-12T10:24:06.628Z","airDate":"2019-01-12T10:24:06.628Z"}],"summary":"string","bangumiUrl":"string","userRating":0,"favoriteStatus":"favorited","comment":"string","ratingDetails":{},"relateds":[{"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true,"rating":0}],"similars":[{"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true,"rating":0}],"tags":[{"id":0,"name":"string"}],"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true,"rating":0}
     */

    private BangumiBean bangumi;

    public BangumiBean getBangumi() {
        return bangumi;
    }

    public void setBangumi(BangumiBean bangumi) {
        this.bangumi = bangumi;
    }

    public static class BangumiBean {
        /**
         * type : tvseries
         * typeDescription : string
         * episodes : [{"episodeId":0,"episodeTitle":"string","lastWatched":"2019-01-12T10:24:06.628Z","airDate":"2019-01-12T10:24:06.628Z"}]
         * summary : string
         * bangumiUrl : string
         * userRating : 0
         * favoriteStatus : favorited
         * comment : string
         * ratingDetails : {}
         * relateds : [{"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true,"rating":0}]
         * similars : [{"animeId":0,"animeTitle":"string","imageUrl":"string","searchKeyword":"string","isOnAir":true,"airDay":0,"isFavorited":true,"isRestricted":true,"rating":0}]
         * tags : [{"id":0,"name":"string"}]
         * animeId : 0
         * animeTitle : string
         * imageUrl : string
         * searchKeyword : string
         * isOnAir : true
         * airDay : 0
         * isFavorited : true
         * isRestricted : true
         * rating : 0
         */

        private String type;
        private String typeDescription;
        private String summary;
        private String bangumiUrl;
        private int userRating;
        private String favoriteStatus;
        private String comment;
        private RatingDetailsBean ratingDetails;
        private int animeId;
        private String animeTitle;
        private String imageUrl;
        private String searchKeyword;
        private boolean isOnAir;
        private int airDay;
        private boolean isFavorited;
        private boolean isRestricted;
        private int rating;
        private List<EpisodesBean> episodes;
        private List<AnimeBean> relateds;
        private List<AnimeBean> similars;
        private List<TagsBean> tags;

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

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
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

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public List<EpisodesBean> getEpisodes() {
            return episodes;
        }

        public void setEpisodes(List<EpisodesBean> episodes) {
            this.episodes = episodes;
        }

        public List<AnimeBean> getRelateds() {
            return relateds;
        }

        public void setRelateds(List<AnimeBean> relateds) {
            this.relateds = relateds;
        }

        public List<AnimeBean> getSimilars() {
            return similars;
        }

        public void setSimilars(List<AnimeBean> similars) {
            this.similars = similars;
        }

        public List<TagsBean> getTags() {
            if (tags == null || tags.size() == 0){
                tags = new ArrayList<>();
                tags.add(new TagsBean(-1, "暂无标签"));
            }
            return tags;
        }

        public void setTags(List<TagsBean> tags) {
            this.tags = tags;
        }

        public static class RatingDetailsBean {
        }

        public static class EpisodesBean {
            /**
             * episodeId : 0
             * episodeTitle : string
             * lastWatched : 2019-01-12T10:24:06.628Z
             * airDate : 2019-01-12T10:24:06.628Z
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

        public static class TagsBean {
            /**
             * id : 0
             * name : string
             */

            private int id;
            private String name;

            public TagsBean(int id, String name) {
                this.id = id;
                this.name = name;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
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

    public static void follow(String animaId, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        Map<String, String> map = new HashMap<>();
        map.put("animeId", animaId);
        map.put("favoriteStatus", "favorited");
        map.put("rating", "0");
        RetroFactory.getInstance().follow(map)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void unFollow(String animaId, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().unFollow(animaId)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
