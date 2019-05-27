package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.database.DataBaseInfo;
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

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
        io.reactivex.Observable.create((ObservableOnSubscribe<List<VideoBean>>) emitter ->
                    emitter.onNext(getDataBaseVideo(folderPath)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoBeanList -> getView().refreshAdapter(videoBeanList));
    }

    @Override
    public void updateDanmu(String danmuPath, int episodeId, String[] whereArgs) {
        new Thread(()->{
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            ContentValues values = new ContentValues();
            values.put("danmu_path", danmuPath);
            values.put("danmu_episode_id", episodeId);
            String whereCase = DataBaseInfo.getFieldNames()[2][1]+" = ? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[2],values, whereCase, whereArgs);
        }).start();
    }

    @Override
    public void deleteFile(String filePath) {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            String folderPath = FileUtils.getDirName(filePath);
            //delete file
            String whereCase = DataBaseInfo.getFieldNames()[2][1]+" = ? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[2], whereCase, new String[]{folderPath, filePath});
            //folder file number reduce, if number-1 == 0, delete folder
            String sql = "SELECT * FROM folder WHERE folder_path = ?";
            Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath});
            if (cursor.moveToNext()){
                int number = cursor.getInt(2);
                if (number > 2){
                    ContentValues values = new ContentValues();
                    values.put(DataBaseInfo.getFieldNames()[1][2], --number);
                    sqLiteDatabase.update(DataBaseInfo.getTableNames()[1], values, "folder_path = ?", new String[]{folderPath});
                }else {
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[1], "folder_path = ?", new String[]{folderPath});
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
        io.reactivex.Observable.create((ObservableOnSubscribe<Boolean>) e -> {
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
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();

        String sql = "SELECT * FROM file WHERE folder_path = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql ,new String[]{folderPath});
        while (cursor.moveToNext()){
            String filePath = cursor.getString(2);
            File file = new File(filePath);
            if (!file.exists()){
                sqLiteDatabase.delete("file", "folder_path = ? AND file_path = ?", new String[]{folderPath, filePath});
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
