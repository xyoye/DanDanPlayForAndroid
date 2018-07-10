package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class HomeFragmentPresenterImpl extends BaseMvpPresenter<HomeFragmentView> implements HomeFragmentPresenter {
    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        DanmuMatchParam param = new DanmuMatchParam();
        param.setFileName("刀剑神域");
        param.setFileHash("feb860735d3e2be9be6ae789962c7ca8");
        param.setFileSize(171772938);
        param.setVideoDuration(1440000);
        param.setMatchMode("hashAndFileName");
        DanmuMatchBean.matchDanmu(param,  new CommJsonObserver<DanmuMatchBean>(){
            @Override
            public void onSuccess(DanmuMatchBean danmuMatchBean) {
                ToastUtils.showShort("匹配成功："+danmuMatchBean.getMatches().get(0).getAnimeTitle());
            }

            @Override
            public void onError(int errorCode, String message) {
                System.out.println(errorCode);
            }
        }, new NetworkConsumer());
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
}
