package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/9.
 */


public class DanmuMatchBean extends CommJsonEntity implements Serializable{

    /**
     * isMatched : true
     * matches : [{"episodeId":0,"animeId":0,"animeTitle":"string","episodeTitle":"string","type":"tvseries","shift":0}]
     */

    private boolean isMatched;
    private List<MatchesBean> matches;

    public boolean isIsMatched() {
        return isMatched;
    }

    public void setIsMatched(boolean isMatched) {
        this.isMatched = isMatched;
    }

    public List<MatchesBean> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchesBean> matches) {
        this.matches = matches;
    }

    public static class MatchesBean {
        /**
         * episodeId : 0
         * animeId : 0
         * animeTitle : string
         * episodeTitle : string
         * type : tvseries
         * shift : 0
         */

        private int episodeId;
        private int animeId;
        private String animeTitle;
        private String episodeTitle;
        private String type;
        private int shift;

        public int getEpisodeId() {
            return episodeId;
        }

        public void setEpisodeId(int episodeId) {
            this.episodeId = episodeId;
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

        public String getEpisodeTitle() {
            return episodeTitle;
        }

        public void setEpisodeTitle(String episodeTitle) {
            this.episodeTitle = episodeTitle;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getShift() {
            return shift;
        }

        public void setShift(int shift) {
            this.shift = shift;
        }
    }

    public static void matchDanmu(DanmuMatchParam param, CommJsonObserver<DanmuMatchBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().matchDanmu(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
