package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.SearchDanmuUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuNetworkPresenterImpl extends BaseMvpPresenter<DanmuNetworkView> implements DanmuNetworkPresenter {

    public DanmuNetworkPresenterImpl(DanmuNetworkView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        getView().showLoading();
        String videoPath = getView().getVideoPath();
        if (StringUtils.isEmpty(videoPath)) return;
        String title = FileUtils.getFileName(videoPath);
        String hash = SearchDanmuUtil.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = SearchDanmuUtil.getVideoDuration(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        param.setFileName(title);
        param.setFileHash(hash);
        param.setFileSize(length);
        param.setVideoDuration(duration);
        param.setMatchMode("hashAndFileName");
        searchDanmu(param);
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
    public void searchDanmu(DanmuMatchParam param) {
        DanmuMatchBean.matchDanmu(param,  new CommJsonObserver<DanmuMatchBean>(){
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
}
