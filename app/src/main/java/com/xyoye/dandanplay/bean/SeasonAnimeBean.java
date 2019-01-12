package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyy on 2019/1/9.
 */

public class SeasonAnimeBean extends CommJsonEntity {


    private List<SeasonsBean> seasons;

    public List<SeasonsBean> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonsBean> seasons) {
        this.seasons = seasons;
    }

    public static class SeasonsBean {
        /**
         * year : 0
         * month : 0
         * seasonName : string
         */

        private int year;
        private int month;
        private String seasonName;

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public String getSeasonName() {
            return seasonName;
        }

        public void setSeasonName(String seasonName) {
            this.seasonName = seasonName;
        }
    }

    public static void getAnimeSeasons(CommJsonObserver<SeasonAnimeBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getAnimeSeason()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void getSeasonAnimas(String year, String month, CommJsonObserver<BangumiBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getSeasonAnime(year, month)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
