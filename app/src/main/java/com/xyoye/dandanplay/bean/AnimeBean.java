package com.xyoye.dandanplay.bean;

import java.io.Serializable;

/**
 * Created by xyoye on 2019/1/12.
 */


public class AnimeBean implements Serializable {
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
