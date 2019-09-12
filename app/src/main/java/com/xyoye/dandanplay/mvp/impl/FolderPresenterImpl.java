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
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/6/30 0030.
 */

public class FolderPresenterImpl extends BaseMvpPresenterImpl<FolderView> implements FolderPresenter {
    private Disposable folderScanDis;

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
        if (folderScanDis != null)
            folderScanDis.dispose();
    }

    @SuppressLint("CheckResult")
    @Override
    public void getVideoList(String folderPath) {
        folderScanDis = Observable.create((ObservableOnSubscribe<List<VideoBean>>) emitter ->
                emitter.onNext(getDataBaseVideo(folderPath)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoBeanList -> getView().refreshAdapter(videoBeanList));
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

    //获取数据库中本地文件列表，如果本地文件不存在，删除记录
    //get local file form database
    private List<VideoBean> getDataBaseVideo(String folderPath) {
        List<VideoBean> videoBeans = new ArrayList<>();
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .where("folder_path", folderPath)
                .execute();
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
            videoBeans.add(videoBean);
        }
        cursor.close();
        return videoBeans;
    }
}
