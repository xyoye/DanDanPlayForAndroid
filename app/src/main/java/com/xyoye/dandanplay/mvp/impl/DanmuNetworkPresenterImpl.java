package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.DanmuSearchBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.MD5Util;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuNetworkPresenterImpl extends BaseMvpPresenterImpl<DanmuNetworkView> implements DanmuNetworkPresenter {

    public DanmuNetworkPresenterImpl(DanmuNetworkView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        getView().showLoading();
        String videoPath = getView().getVideoPath();

        if (StringUtils.isEmpty(videoPath)) return;
        if (!getView().isLan()){
            String title = FileUtils.getFileName(videoPath);
            String hash = MD5Util.getVideoFileHash(videoPath);
            long length = new File(videoPath).length();
            long duration = MD5Util.getVideoDuration(videoPath);
            DanmuMatchParam param = new DanmuMatchParam();
            param.setFileName(title);
            param.setFileHash(hash);
            param.setFileSize(length);
            param.setVideoDuration(duration);
            param.setMatchMode("hashAndFileName");
            matchDanmu(param);
        }else {
            String anime = FileUtils.getFileNameNoExtension(videoPath);
            searchDanmu(anime, "");
        }
    }

    @Override
    public void process(Bundle savedInstanceState) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void matchDanmu(DanmuMatchParam param) {
        DanmuMatchBean.matchDanmu(param,  new CommJsonObserver<DanmuMatchBean>(getLifeful()){
            @Override
            public void onSuccess(DanmuMatchBean danmuMatchBean) {
                getView().hideLoading();
                if (danmuMatchBean.getMatches().size() > 0)
                    getView().refreshAdapter(danmuMatchBean.getMatches());
                else
                    ToastUtils.showShort("无匹配弹幕");
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void searchDanmu(String anime, String episode) {
        getView().showLoading();
        DanmuSearchBean.searchDanmu(anime, episode, new CommJsonObserver<DanmuSearchBean>(getLifeful()){
            @Override
            public void onSuccess(DanmuSearchBean danmuSearchBean) {
                getView().hideLoading();
                if (danmuSearchBean.getAnimes().size() > 0){
                    List<DanmuMatchBean.MatchesBean> matchesBeanList = new ArrayList<>();
                    for (DanmuSearchBean.AnimesBean animesBean : danmuSearchBean.getAnimes()){
                        DanmuMatchBean.MatchesBean matchesBean = new DanmuMatchBean.MatchesBean();
                        for (DanmuSearchBean.AnimesBean.EpisodesBean episodesBean : animesBean.getEpisodes()){
                            matchesBean.setAnimeId(animesBean.getAnimeId());
                            matchesBean.setAnimeTitle(animesBean.getAnimeTitle());
                            matchesBean.setType(animesBean.getType());
                            matchesBean.setEpisodeId(episodesBean.getEpisodeId());
                            matchesBean.setEpisodeTitle(episodesBean.getEpisodeTitle());
                            matchesBeanList.add(matchesBean);
                        }
                    }
                    getView().refreshAdapter(matchesBeanList);
                }else
                    ToastUtils.showShort("无匹配弹幕");
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }


}
