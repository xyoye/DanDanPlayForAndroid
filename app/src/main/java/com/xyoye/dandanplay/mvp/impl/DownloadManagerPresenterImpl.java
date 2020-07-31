package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ServiceUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.utils.RxUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadManagerPresenterImpl extends BaseMvpPresenterImpl<DownloadManagerView> implements DownloadManagerPresenter {

    private Disposable serviceDis = null;

    public DownloadManagerPresenterImpl(DownloadManagerView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
        if (serviceDis != null)
            serviceDis.dispose();
    }

    @Override
    public void observeService() {
        //等待服务开启后增加新任务
        getView().showLoading();
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int waitTime = 0;
            while (true) {
                try {
                    if (ServiceUtils.isServiceRunning(TorrentService.class)) {
                        e.onNext(true);
                        break;
                    }
                    if (waitTime > 10) {
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
                .as(RxUtils.bindLifecycle(getLifecycle()))
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        getView().startNewTask();
                        getView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().hideLoading();
                        getView().showError("开启下载服务失败");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
