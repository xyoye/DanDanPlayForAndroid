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
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
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
        FindVideoTask findVideoTask = new FindVideoTask();
        findVideoTask.setQueryListener(new FindVideoTask.QueryListener() {
            @Override
            public void onResult(List<VideoBean> videoList) {
                for (VideoBean videoBean : videoList){
                    String folderPath = videoBean.getVideoPath();
                    String fileName = videoBean.getVideoName();
                    long duration = videoBean.getVideoDuration();
                    saveData(folderPath,fileName, duration);
                }
                getView().refreshAdapter(getFolderList());
            }
        });
        findVideoTask.execute(getApplicationContext());
    }

    @Override
    public void listFolder(String path) {
        File file = new File(path);
        final FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        Observable.just(file)
                .flatMap(new Function<File, Observable<File>>() {
                    @Override
                    public Observable<File> apply(File file) {
                        return listFiles(file);
                    }
                })
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
            return Observable.just(f).filter(new Predicate<File>() {
                @Override
                public boolean test(File file) {
                    return false;
                }
            });
        }
        if(f.isDirectory()){
            return Observable.fromArray(f.listFiles()).flatMap(new Function<File, Observable<File>>() {
                @Override
                public Observable<File> apply(File file) {
                    return listFiles(file);
                }
            });
        } else {
            return Observable.just(f).filter(new Predicate<File>() {
                @Override
                public boolean test(File file) {
                    return f.exists() && f.canRead() && isVideo(f);
                }
            });
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
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT folder_path,count(folder_path) AS number FROM file GROUP BY folder_path",new String[]{});
        while (cursor.moveToNext()){
            String folderPath = cursor.getString(0);
            int fileNumber = cursor.getInt(1);
            folderBeanList.add(new FolderBean(folderPath,fileNumber));

            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[1][1],folderPath);
            values.put(DataBaseInfo.getFieldNames()[1][2],fileNumber);
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[1], values,null,null);
        }
        cursor.close();
        return folderBeanList;
    }
}
