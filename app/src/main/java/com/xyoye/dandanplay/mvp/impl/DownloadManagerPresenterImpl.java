package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.blankj.utilcode.util.ServiceUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.utils.Lifeful;

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/10/27.
 */


public class DownloadManagerPresenterImpl extends BaseMvpPresenterImpl<DownloadManagerView> implements DownloadManagerPresenter {

    public DownloadManagerPresenterImpl(DownloadManagerView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

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
    public void getTorrentList() {

    }

    @SuppressLint("CheckResult")
    @Override
    public void observeService() {
        //等待服务开启后增加新任务
        getView().showLoading();
        io.reactivex.Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int waitTime = 0;
            while (true){
                try {
                    if(ServiceUtils.isServiceRunning(TorrentService.class)){
                        getView().hideLoading();
                        e.onNext(true);
                        e.onComplete();
                        break;
                    }
                    if (waitTime > 10){
                        getView().hideLoading();
                        getView().showError("开启下载服务失败");
                        break;
                    }
                    waitTime++;
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean ->
                        getView().startNewTask());
    }
}
