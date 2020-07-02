package com.xyoye.dandanplay.bean;

import android.text.TextUtils;

import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.utils.MD5Util;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.smb.SmbManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/9.
 */

public class DanmuMatchBean extends CommJsonEntity implements Serializable {

    /**
     * isMatched : true
     * matches : [{"episodeId":0,"animeId":0,"animeTitle":"string","episodeTitle":"string","type":"tvseries","shift":0}]
     */

    private boolean isMatched;
    private List<MatchesBean> matches;

    private String videoPath;

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

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
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

    public static void matchDanmu(DanmuMatchParam param, CommJsonObserver<DanmuMatchBean> observer, NetworkConsumer consumer) {
        RetroFactory.getInstance().matchDanmu(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void matchSmbDanmu(String videoName, CommJsonObserver<DanmuMatchBean> observer, NetworkConsumer consumer) {
        Observable.just(videoName).map(name -> {
            DanmuMatchParam param = new DanmuMatchParam();
            String videoHash = getSmbVideoHash(name);
            if (!TextUtils.isEmpty(videoHash)){
                param.setFileHash(videoHash);
                param.setMatchMode("hashOnly");
            }
            return param;
        }).flatMap((Function<DanmuMatchParam, ObservableSource<DanmuMatchBean>>) danmuMatchParam ->
                RetroFactory.getInstance().matchDanmu(danmuMatchParam.getMap())
        ).doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 获取视频的Hash
     */
    private static String getSmbVideoHash(String videoName){
        InputStream inputStream = SmbManager.getInstance().getController().getFileInputStream(videoName);
        if (inputStream == null) {
            throw new RuntimeException("Can not open video stream: "+videoName);
        }

        byte[] bytes = new byte[1024 * 1024];
        int totalLength = 0;
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 1024 * 1024);
            int length = 0;
            while ((length = inputStream.read(bytes, 0, bytes.length)) > 0){
                byteBuffer.put(bytes);
                totalLength += length;
                if (totalLength >= byteBuffer.limit()){
                    break;
                }
            }
            if (length > -1) {
                return MD5Util.getMD5String(byteBuffer.array());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
