package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ScalarCallable;
import io.reactivex.schedulers.Schedulers;

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

    @SuppressLint("CheckResult")
    @Override
    public void getVideoFormSystem() {
        String[] projection = new String[]{ MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION};
        Observable.just(getApplicationContext())
                .map(context -> {
                    try {
                        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                projection, null, null, null);
                        if (cursor == null) return getFolderList();
                        while (cursor.moveToNext()) {

                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));// 地址
                            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// id
                            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长

                            VideoBean videoBean = new VideoBean();
                            videoBean.set_id(_id);
                            videoBean.setVideoPath(path);
                            videoBean.setVideoDuration(duration);
                            videoBean.setVideoSize(size);
                            saveData(videoBean);
                        }
                        cursor.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return getFolderList();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoList ->
                        getView().refreshAdapter(videoList));
    }

    @SuppressLint("CheckResult")
    @Override
    public void getVideoFormSystemAndSave() {
        Observable.just(getView().getContext())
                //刷新媒体文件
                .map(this::queryVideoFormMediaStore)
                //获取保存的扫描文件夹
                .map(over -> queryVideoFormPath())
                //遍历文件夹
                .flatMap(
                        (Function<File[], ObservableSource<File>>) fileList -> Observable
                        .fromArray(fileList)
                        .flatMap(this::listFiles)
                )
                //保存视频文件信息
                .map(file -> {
                    String filePath = file.getAbsolutePath();
                    VideoBean videoBean = new VideoBean();
                    videoBean.setVideoPath(filePath);
                    videoBean.setVideoDuration(0);
                    videoBean.setVideoSize(file.length());
                    videoBean.set_id(0);
                    saveData(videoBean);
                    return getFolderList();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FolderBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<FolderBean> folderBeans) {
                       getView().refreshAdapter(folderBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        getView().refreshOver();
                    }
                });
    }

    private boolean queryVideoFormMediaStore(Context context){
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));// 地址
                int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// id
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长

                VideoBean videoBean = new VideoBean();
                videoBean.set_id(_id);
                videoBean.setVideoPath(path);
                videoBean.setVideoDuration(duration);
                videoBean.setVideoSize(size);
                saveData(videoBean);
            }
            cursor.close();
        }
        return true;
    }

    private File[] queryVideoFormPath(){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor folderCursor = sqLiteDatabase.query(DataBaseInfo.getTableNames()[11], null, null, null, null, null, null);
        File[] files;
        int count = 0;
        if (folderCursor.getCount() == 0)
            return new File[]{};
        else
            files = new File[folderCursor.getCount()];

        while (folderCursor.moveToNext()) {
            folderCursor.getCount();
            files[count] = new File(folderCursor.getString(1));
            count ++;
        }
        folderCursor.close();
        return files;
    }

    @SuppressLint("CheckResult")
    public void getVideoFormDatabase(){
        io.reactivex.Observable
                .create((ObservableOnSubscribe<List<FolderBean>>) emitter ->
                        emitter.onNext(getFolderList()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoList -> getView().refreshAdapter(videoList));
    }

    //遍历视频文件
    private Observable<File> listFiles(final File f){
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

    //保存数据库中不存在的视频信息
    private void saveData(VideoBean videoBean){
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
        }
        cursor.close();
    }

    //从数据库查询文件夹数据
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
