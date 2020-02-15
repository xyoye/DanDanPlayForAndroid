package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.MD5Util;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/6/30 0030.
 */

public class FolderPresenterImpl extends BaseMvpPresenterImpl<FolderView> implements FolderPresenter {

    public FolderPresenterImpl(FolderView view, Lifeful lifeful) {
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

    @SuppressLint("CheckResult")
    @Override
    public void getVideoList(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .where("folder_path", folderPath)
                .postExecute(new QueryAsyncResultCallback<List<VideoBean>>(getLifeful()) {
                    @Override
                    public List<VideoBean> onQuery(Cursor cursor) {
                        if (cursor == null) return new ArrayList<>();
                        List<VideoBean> videoBeans = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            String filePath = cursor.getString(2);
                            File file = new File(filePath);
                            if (!file.exists()) {
                                DataBaseManager.getInstance()
                                        .selectTable("file")
                                        .delete()
                                        .where("folder_path", folderPath)
                                        .where("file_path", filePath)
                                        .postExecute();
                                continue;
                            }

                            VideoBean videoBean = new VideoBean();
                            videoBean.setVideoPath(filePath);
                            videoBean.setDanmuPath(cursor.getString(3));
                            videoBean.setCurrentPosition(cursor.getInt(4));
                            videoBean.setVideoDuration(Long.parseLong(cursor.getString(5)));
                            videoBean.setEpisodeId(cursor.getInt(6));
                            videoBean.setVideoSize(Long.parseLong(cursor.getString(7)));
                            videoBean.set_id(cursor.getInt(8));
                            videoBean.setZimuPath(cursor.getString(9));
                            videoBeans.add(videoBean);
                        }
                        return videoBeans;
                    }

                    @Override
                    public void onResult(List<VideoBean> result) {
                        getView().refreshAdapter(result);
                    }
                });
    }

    @Override
    public void updateDanmu(String danmuPath, int episodeId, String[] whereArgs) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("danmu_path", danmuPath)
                .param("danmu_episode_id", episodeId)
                .where("folder_path", whereArgs[0])
                .where("file_path", whereArgs[1])
                .postExecute();
    }

    @Override
    public void updateZimu(String zimuPath, String[] whereArgs) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("zimu_path", zimuPath)
                .where("folder_path", whereArgs[0])
                .where("file_path", whereArgs[1])
                .postExecute();
    }

    @Override
    public void getDanmu(String videoPath) {
        getView().showLoading();
        String title = FileUtils.getFileName(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        String hash = MD5Util.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = MD5Util.getVideoDuration(videoPath);
        param.setFileName(title);
        param.setFileHash(hash);
        param.setFileSize(length);
        param.setVideoDuration(duration);
        param.setMatchMode("hashAndFileName");
        DanmuMatchBean.matchDanmu(param, new CommJsonObserver<DanmuMatchBean>(getLifeful()) {
            @Override
            public void onSuccess(DanmuMatchBean danmuMatchBean) {
                getView().hideLoading();
                if (danmuMatchBean.getMatches().size() > 0)
                    getView().downloadDanmu(danmuMatchBean.getMatches().get(0));
                else
                    getView().noMatchDanmu(videoPath);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().noMatchDanmu(videoPath);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void bindAllDanmu(List<VideoBean> videoList) {
        Observable.just(videoList)
                .map(videoBeans -> {
                    Set<Observable<DanmuMatchBean>> requestList = new HashSet<>();
                    for (VideoBean videoBean : videoBeans) {
                        Map<String, String> danmuMatchParam = getDanmuMatchParam(videoBean.getVideoPath());
                        requestList.add(
                                RetroFactory.getInstance().matchDanmu(danmuMatchParam)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        //请求成功时，将视频地址放入结果，用于最后的匹配
                                        .doOnNext(danmuMatchBean -> danmuMatchBean.setVideoPath(videoBean.getVideoPath()))
                                        //请求错误时不抛出，返回不为NULL的空对象
                                        .onErrorReturnItem(new DanmuMatchBean()));
                    }

                    return Observable.zipIterable(requestList, resultObjectArray -> {
                        List<DanmuMatchBean> resultBeanList = new ArrayList<>();
                        for (Object result : resultObjectArray) {
                            DanmuMatchBean resultBean = (DanmuMatchBean) result;
                            //排除请求错误时，产生的不为NULL的空对象
                            if (resultBean == null)
                                continue;
                            resultBeanList.add(resultBean);
                        }
                        return resultBeanList;
                    }, true, 1);
                })
                .flatMap((Function<Observable<List<DanmuMatchBean>>, ObservableSource<List<DanmuMatchBean>>>)
                        listObservable -> listObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DanmuMatchBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<DanmuMatchBean> danmuMatchBeans) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private Map<String, String> getDanmuMatchParam(String videoPath) {
        String title = FileUtils.getFileName(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        String hash = MD5Util.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = MD5Util.getVideoDuration(videoPath);
        param.setFileName(title);
        param.setFileHash(hash);
        param.setFileSize(length);
        param.setVideoDuration(duration);
        param.setMatchMode("hashAndFileName");
        return param.getMap();
    }

    @Override
    public void unbindAllDanmu(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("danmu_path", "")
                .where("folder_path", folderPath)
                .postExecute();

        getVideoList(folderPath);
    }

    @Override
    public void bindAllZimu(List<VideoBean> videoList) {

    }

    @Override
    public void unbindAllZimu(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("zimu_path", "")
                .where("folder_path", folderPath)
                .postExecute();

        getVideoList(folderPath);
    }

    private void bindDanmu(int position, String videoPath) {

    }
}
