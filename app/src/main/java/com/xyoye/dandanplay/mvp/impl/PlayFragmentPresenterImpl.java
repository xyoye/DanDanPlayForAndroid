package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.utils.Config;
import com.xyoye.dandanplay.utils.FindVideoTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PlayFragmentPresenterImpl extends BaseMvpPresenter<PlayFragmentView> implements PlayFragmentPresenter {

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
                String folderPath = videoBean.getVideoPath();
                String fileName = videoBean.getVideoName();
                long duration = videoBean.getVideoDuration();
                saveData(folderPath,fileName, duration);
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

    @Override
    public void listFolder(String path) {
        File file = new File(path);
        final FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        Observable.just(file)
                .flatMap((Function<File, Observable<File>>) this::listFiles)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                               @Override
                               public void onSubscribe(Disposable d) {

                               }

                               @Override
                               public void onNext(File file) {
                                   try {
                                       String folderPath = FileUtils.getDirName(file);
                                       String fileName = FileUtils.getFileName(file);
                                       fmmr.setDataSource(folderPath +fileName);
                                       long duration = Long.parseLong(fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION));
                                       saveData(folderPath, fileName, duration);
                                   }catch (Exception e){
                                       e.printStackTrace();
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                    e.printStackTrace();
                               }

                               @Override
                               public void onComplete() {
                                   fmmr.release();
                                   getView().refreshAdapter(getFolderList());
                               }
                           });
    }

    /**
     * RxJava递归查询内存中的视频文件
     */
    private Observable<File> listFiles(final File f){
        String name = FileUtils.getFileName(f.getAbsolutePath());
        if ("ANDROID".equals(name.toUpperCase()) ||
                name.startsWith("com") ||
                name.startsWith(".")){
            return Observable.just(f).filter(file -> false);
        }
        if(f.isDirectory()){
            return Observable.fromArray(f.listFiles()).flatMap((Function<File, Observable<File>>) this::listFiles);
        } else {
            return Observable.just(f).filter(file -> f.exists() && f.canRead() && isVideo(f));
        }
    }

    private static boolean isVideo(File file){
        String path = file.getAbsolutePath();
        String ext = FileUtils.getFileExtension(path).toUpperCase();
        return Config.videoType.contains(ext);
    }

    private void saveData(String folderPath, String fileName, long duration){
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], fileName);
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(duration));
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[2]+
                " WHERE "+DataBaseInfo.getFieldNames()[2][1]+ "=? " +
                "AND "+DataBaseInfo.getFieldNames()[2][2]+ "=? ";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, fileName});
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

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT folder_path, file_name FROM file",new String[]{});
        while (cursor.moveToNext()){
            String folderPath = cursor.getString(0);
            String fileName = cursor.getString(1);
            String filePath = folderPath + "/" + fileName;

            File file = new File(filePath);
            if (file.exists()){
                if (beanMap.containsKey(folderPath)){
                    int number = beanMap.get(folderPath);
                    beanMap.put(folderPath, ++number);
                }else {
                    beanMap.put(folderPath, 1);
                }
            }else {
                deleteMap.put(folderPath, fileName);
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
            sqLiteDatabase.delete("file", "folder_path=? AND file_name = ?" , new String[]{entry.getKey(), entry.getValue()});
        }
        return folderBeanList;
    }
}
