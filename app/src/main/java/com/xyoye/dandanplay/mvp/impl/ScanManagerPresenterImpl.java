package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.utils.database.DataBaseInfo;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.ScanManagerPresenter;
import com.xyoye.dandanplay.mvp.view.ScanManagerView;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/5/14.
 */

public class ScanManagerPresenterImpl extends BaseMvpPresenterImpl<ScanManagerView> implements ScanManagerPresenter {
    private Disposable videoScanDis;
    private int newAddFileCount = 0;

    public ScanManagerPresenterImpl(ScanManagerView view, Lifeful lifeful) {
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
        if (videoScanDis != null)
            videoScanDis.dispose();
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

        Cursor cursor = DataBaseManager.getInstance()
                        .selectTable(2)
                        .query()
                        .where(1, folderPath)
                        .where(2, videoBean.getVideoPath())
                        .execute();

        if (!cursor.moveToNext()) {
            DataBaseManager.getInstance()
                    .selectTable(2)
                    .insert()
                    .param(1, folderPath)
                    .param(2, videoBean.getVideoPath())
                    .param(5, String.valueOf(videoBean.getVideoDuration()))
                    .param(7, String.valueOf(videoBean.getVideoSize()))
                    .param(8, videoBean.get_id())
                    .execute();
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    public void listFolder(String rootFilePath) {
        newAddFileCount = 0;
        videoScanDis = Observable.just(new File(rootFilePath))
                .map(rootFile -> {
                    for (File childFile : listFiles(rootFile)){
                        VideoBean videoBean = new VideoBean();
                        queryFormSystem(videoBean, childFile.getAbsolutePath());
                        if (saveNewVideo(videoBean)){
                            newAddFileCount++;
                        }
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_ADAPTER_DATA));
                    ToastUtils.showShort("扫描完成，共新增："+newAddFileCount+"个视频");
                });
    }

    /**
     * 递归检查目录和文件
     */
    private List<File> listFiles(File file){
        List<File> fileList = new ArrayList<>();
        if(file.isDirectory()){
            File[] fileArray = file.listFiles();
            if (fileArray == null || fileArray.length == 0){
                return new ArrayList<>();
            }else {
                for (File childFile : fileArray) {
                    if (childFile.isDirectory()) {
                        fileList.addAll(listFiles(childFile));
                    } else if (childFile.exists() && childFile.canRead() && CommonUtils.isMediaFile(childFile.getAbsolutePath())) {
                        fileList.add(childFile);
                    }
                }
            }
        } else if (file.exists() && file.canRead() && CommonUtils.isMediaFile(file.getAbsolutePath())){
            fileList.add(file);
        }
        return fileList;
    }
}
