package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyy on 2018/10/24.
 */

public class DanmuSearchBean extends CommJsonEntity{


    /**
     * hasMore : true
     * animes : [{"animeId":0,"animeTitle":"string","type":"tvseries","typeDescription":"string","episodes":[{"episodeId":0,"episodeTitle":"string"}]}]
     */

    private boolean hasMore;
    private List<AnimesBean> animes;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

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
         * episodes : [{"episodeId":0,"episodeTitle":"string"}]
         */

        private int animeId;
        private String animeTitle;
        private String type;
        private String typeDescription;
        private List<EpisodesBean> episodes;

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

        public List<EpisodesBean> getEpisodes() {
            return episodes;
        }

        public void setEpisodes(List<EpisodesBean> episodes) {
            this.episodes = episodes;
        }

        public static class EpisodesBean {
            /**
             * episodeId : 0
             * episodeTitle : string
             */

            private int episodeId;
            private String episodeTitle;

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
        }
    }

    public static void searchDanmu(String anime, String episode, CommJsonObserver<DanmuSearchBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().searchDanmu(anime, episode)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
