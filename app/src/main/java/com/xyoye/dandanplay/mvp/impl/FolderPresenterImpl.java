package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.utils.HashBufferedInputStream;
import com.xyoye.dandanplay.utils.SearchDanmuUtil;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jcifs.smb.SmbFile;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderPresenterImpl extends BaseMvpPresenter<FolderView> implements FolderPresenter {
    // is smb file view
    private boolean isLan;

    public FolderPresenterImpl(FolderView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        isLan = getView().isLan();
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
                    emitter.onNext(isLan ?
                            getLanVideo(folderPath) :
                            getDataBaseVideo(folderPath)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoBeanList -> getView().refreshAdapter(videoBeanList));
    }

    @Override
    public void updateDanmu(String danmuPath, int episodeId, String[] whereArgs) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values = new ContentValues();
        values.put("danmu_path", danmuPath);
        values.put("danmu_episode_id", episodeId);
        String whereCase;
        if (!isLan){
            whereCase = DataBaseInfo.getFieldNames()[2][1]+" = ? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[2],values, whereCase, whereArgs);
        }else {
            whereCase = DataBaseInfo.getFieldNames()[7][1]+" = ? AND "+ DataBaseInfo.getFieldNames()[7][2]+" =? ";
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[7],values, whereCase, whereArgs);
        }
    }

    @Override
    public void updateCurrent(SaveCurrentEvent event) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values = new ContentValues();
        values.put("current_position", event.getCurrentPosition());
        String whereCase;
        if (!isLan){
            whereCase = DataBaseInfo.getFieldNames()[2][1]+" =? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[2], values, whereCase, new String[]{event.getFolderPath(), event.getVideoPath()});
        }else {
            whereCase = DataBaseInfo.getFieldNames()[7][1]+" = ? AND "+ DataBaseInfo.getFieldNames()[7][2]+" =? ";
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[7], values, whereCase, new String[]{event.getFolderPath(), event.getVideoPath()});
        }
    }

    @Override
    public void deleteFile(String filePath) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        if (!isLan){
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
        }else {
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[7], "file_path = ?", new String[]{filePath});
        }
    }

    @Override
    public void getDanmu(String videoPath){
        String title = FileUtils.getFileName(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        String hash = SearchDanmuUtil.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = SearchDanmuUtil.getVideoDuration(videoPath);
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
                ToastUtils.showShort(message);
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
                sqLiteDatabase.delete("file", "folder_path = ? AND file_name = ?", new String[]{folderPath, filePath});
                continue;
            }

            String danmuPath = cursor.getString(3);
            int currentPosition = cursor.getInt(4);
            long duration = Long.parseLong(cursor.getString(5));
            int episodeId = cursor.getInt(6);
            videoBeans.add(new VideoBean(filePath, danmuPath, currentPosition, duration, episodeId));
        }
        cursor.close();
        return videoBeans;
    }

    //获取数据库中共享文件记录
    //get share file form database
    private List<VideoBean> getLanVideo(String folderPath){
        List<VideoBean> videoBeans = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();

        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[7]+" WHERE folder= ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql ,new String[]{folderPath});
        while (cursor.moveToNext()){
            String filePath = cursor.getString(2);

            String danmuPath = cursor.getString(3);
            int currentPosition = cursor.getInt(4);
            int episodeId = cursor.getInt(5);
            videoBeans.add(new VideoBean(filePath, true, 0,  danmuPath, currentPosition, episodeId));
        }
        cursor.close();
        return videoBeans;
    }
}
