package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.MD5Util;
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
    private Disposable folderScanDis, serviceDis;

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
        if (serviceDis != null)
            serviceDis.dispose();
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
                .selectTable(2)
                .update()
                .param(3, danmuPath)
                .param(6, episodeId)
                .where(1, whereArgs[0])
                .where(2, whereArgs[1])
                .postExecute();
    }

    @Override
    public void deleteFile(String filePath) {
        new Thread(() -> {
            String folderPath = FileUtils.getDirName(filePath);
            //delete file
            DataBaseManager.getInstance()
                    .selectTable(2)
                    .delete()
                    .where(1, folderPath)
                    .where(2, filePath)
                    .execute();

            //folder file number reduce, if number-1 == 0, delete folder
            Cursor cursor = DataBaseManager.getInstance()
                    .selectTable(1)
                    .query()
                    .setColumns(2)
                    .where(1, folderPath)
                    .execute();

            //if folder exist
            if (cursor.moveToNext()){
                int number = cursor.getInt(0);
                if (number > 2){
                    DataBaseManager.getInstance()
                            .selectTable(1)
                            .update()
                            .param(2, --number)
                            .where(1, folderPath)
                            .execute();
                }else {
                    DataBaseManager.getInstance()
                            .selectTable(1)
                            .delete()
                            .where(1, folderPath)
                            .execute();
                }
            }
            cursor.close();
        }).start();
    }

    @Override
    public void getDanmu(String videoPath){
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
        DanmuMatchBean.matchDanmu(param,  new CommJsonObserver<DanmuMatchBean>(getLifeful()){
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

    @SuppressLint("CheckResult")
    @Override
    //waiting 10s to start smbService
    public void observeService(VideoBean videoBean) {
        getView().showLoading();
        serviceDis = Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int waitTime = 0;
            while (true){
                try {
                    if(ServiceUtils.isServiceRunning(SmbService.class)){
                        getView().hideLoading();
                        e.onNext(true);
                        break;
                    }
                    if (waitTime > 10){
                        getView().hideLoading();
                        getView().showError("开启播放服务失败");
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
                        getView().openIntentVideo(videoBean));
    }

    //获取数据库中本地文件列表，如果本地文件不存在，删除记录
    //get local file form database
    private List<VideoBean> getDataBaseVideo(String folderPath){
        List<VideoBean> videoBeans = new ArrayList<>();
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(2)
                .query()
                .where(1, folderPath)
                .execute();
        while (cursor.moveToNext()){
            String filePath = cursor.getString(2);
            File file = new File(filePath);
            if (!file.exists()){
                DataBaseManager.getInstance()
                        .selectTable(2)
                        .delete()
                        .where(1, folderPath)
                        .where(2, filePath)
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
