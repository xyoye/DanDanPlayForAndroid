package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.RefreshFolderEvent;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.VideoScanPresenter;
import com.xyoye.dandanplay.mvp.view.VideoScanView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/5/14.
 */

public class VideoScanPresenterImpl extends BaseMvpPresenterImpl<VideoScanView> implements VideoScanPresenter {

    public VideoScanPresenterImpl(VideoScanView view, Lifeful lifeful) {
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

    //查询系统中是否保存对应视频数据
    @Override
    public void queryFormSystem(VideoBean videoBean, String path){
        Cursor cursor = getView().getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION},
                MediaStore.Video.Media.DATA+" = ?",
                new String[]{path}, null);
        File file = new File(path);
        videoBean.setVideoPath(path);
        videoBean.setVideoSize(file.length());
        if (cursor != null && cursor.moveToNext()){
            videoBean.setVideoDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
            videoBean.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
            cursor.close();
        }else {
            if (cursor != null)
                cursor.close();
            videoBean.setVideoDuration(0);
            videoBean.set_id(0);
        }
    }

    @Override
    public boolean saveNewVideo(VideoBean videoBean) {
        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], videoBean.getVideoPath());
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(videoBean.getVideoDuration()));
        values.put(DataBaseInfo.getFieldNames()[2][7], String.valueOf(videoBean.getVideoSize()));
        values.put(DataBaseInfo.getFieldNames()[2][8], videoBean.get_id());
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[2]+
                " WHERE "+DataBaseInfo.getFieldNames()[2][1]+ "=? " +
                "AND "+DataBaseInfo.getFieldNames()[2][2]+ "=? ";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, videoBean.getVideoPath()});
        if (!cursor.moveToNext()) {
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[2], null, values);
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    public void listFolder(String path) {
        File rootFile = new File(path);
        Observable.just(rootFile)
                .flatMap(this::listFiles)
                .map(file -> {
                    VideoBean videoBean = new VideoBean();
                    queryFormSystem(videoBean, file.getAbsolutePath());
                    return saveNewVideo(videoBean);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    EventBus.getDefault().post(new RefreshFolderEvent(true));
                    ToastUtils.showShort("扫描完成");
                });
    }

    //递归查询内存中的视频文件
    private Observable<File> listFiles(File f){
        if(f.isDirectory()){
            File[] files = f.listFiles();
            if (files == null)
                files = new File[0];
            return Observable
                    .fromArray(files)
                    .flatMap(this::listFiles);
        } else {
            return Observable
                    .just(f)
                    .filter(file -> file != null && f.exists() && f.canRead() && CommonUtils.isMediaFile(f.getAbsolutePath()));
        }
    }
}
