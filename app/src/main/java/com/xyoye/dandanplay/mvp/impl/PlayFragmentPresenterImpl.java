package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.FindVideoTask;
import com.xyoye.dandanplay.utils.Lifeful;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PlayFragmentPresenterImpl extends BaseMvpPresenterImpl<PlayFragmentView> implements PlayFragmentPresenter {

    public PlayFragmentPresenterImpl(PlayFragmentView view, Lifeful lifeful) {
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
    public void getVideoList() {
        getView().showLoading();
        FindVideoTask findVideoTask = new FindVideoTask();
        findVideoTask.setQueryListener(videoList -> {
            for (VideoBean videoBean : videoList){
                String filePath = videoBean.getVideoPath();
                String folderPath = FileUtils.getDirName(filePath);
                long duration = videoBean.getVideoDuration();
                long fileSize = videoBean.getVideoSize();
                int fileId = videoBean.get_id();
                saveData(folderPath, filePath, duration, fileSize, fileId);
            }
            getView().hideLoading();
            getView().refreshAdapter(getFolderList());
        });
        findVideoTask.execute(getApplicationContext());
    }

    @Override
    public void deleteFolder(String folderPath) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        sqLiteDatabase.delete("file", "folder_path = ?" , new String[]{folderPath});
        sqLiteDatabase.delete("folder", "folder_path = ?" , new String[]{folderPath});
        getView().refreshAdapter(getFolderList());
    }

    @SuppressLint("CheckResult")
    @Override
    public void listFolder(String path) {
        File file = new File(path);
        final FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        Observable.just(file)
                .flatMap(this::listFiles)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file1 -> {
                    try {
                        String filePath = file1.getAbsolutePath();
                        String folderPath = FileUtils.getDirName(filePath);
                        fmmr.setDataSource(filePath);
                        long duration = Long.parseLong(fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION));
                        saveData(folderPath, filePath, duration, file1.length(), 0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    fmmr.release();
                    getView().refreshAdapter(getFolderList());
                });
    }

    /**
     * RxJava递归查询内存中的视频文件
     */
    private Observable<File> listFiles(final File f){
        String name = FileUtils.getFileName(f.getAbsolutePath()).toUpperCase();
        if ("ANDROID".equals(name) ||
                name.startsWith("COM") ||
                name.startsWith(".")){
            return Observable.just(f).filter(file -> false);
        }
        if(f.isDirectory()){
            return Observable
                    .fromArray(f.listFiles())
                    .flatMap(this::listFiles);
        } else {
            return Observable
                    .just(f)
                    .filter(file -> f.exists() && f.canRead() && CommonUtils.isMediaFile(f.getAbsolutePath()));
        }
    }

    private void saveData(String folderPath, String filePath, long duration, long fileSize, int fileId){
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], filePath);
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(duration));
        values.put(DataBaseInfo.getFieldNames()[2][7], String.valueOf(fileSize));
        values.put(DataBaseInfo.getFieldNames()[2][8], fileId);
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[2]+
                " WHERE "+DataBaseInfo.getFieldNames()[2][1]+ "=? " +
                "AND "+DataBaseInfo.getFieldNames()[2][2]+ "=? ";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, filePath});
        if (!cursor.moveToNext()) {
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[2], null, values);
        }
        cursor.close();
    }

    private List<FolderBean> getFolderList(){
        List<FolderBean> folderBeanList = new ArrayList<>();
        Map<String, Integer> beanMap = new HashMap<>();
        Map<String, String> deleteMap = new HashMap<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT folder_path, file_path FROM file",new String[]{});
        while (cursor.moveToNext()){
            String folderPath = cursor.getString(0);
            String filePath = cursor.getString(1);

            File file = new File(filePath);
            if (file.exists()){
                if (beanMap.containsKey(folderPath)){
                    int number = beanMap.get(folderPath);
                    beanMap.put(folderPath, ++number);
                }else {
                    beanMap.put(folderPath, 1);
                }
            }else {
                deleteMap.put(folderPath, filePath);
            }
        }
        cursor.close();

        for (Map.Entry<String, Integer> entry : beanMap.entrySet()){
            folderBeanList.add(new FolderBean(entry.getKey(), entry.getValue()));
            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[1][1], entry.getKey());
            values.put(DataBaseInfo.getFieldNames()[1][2], entry.getValue());
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[1], values,null,null);
        }

        for (Map.Entry<String, String> entry : deleteMap.entrySet()){
            sqLiteDatabase.delete("file", "folder_path=? AND file_path = ?" , new String[]{entry.getKey(), entry.getValue()});
        }
        return folderBeanList;
    }
}
