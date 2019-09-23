package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.mvp.presenter.ScanManagerPresenter;
import com.xyoye.dandanplay.mvp.view.ScanManagerView;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/5/14.
 */

public class ScanManagerPresenterImpl extends BaseMvpPresenterImpl<ScanManagerView> implements ScanManagerPresenter {
    private int newAddCount = 0;

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

    }

    @Override
    public void saveNewVideo(List<String> pathList) {
        newAddCount = 0;
        IApplication.getSqlThreadPool().execute(() -> {
            for (String videoPath : pathList) {
                String folderPath = FileUtils.getDirName(videoPath);
                DataBaseManager.getInstance()
                        .selectTable("file")
                        .query()
                        .where("folder_path", folderPath)
                        .where("file_path", videoPath)
                        .executeAsync(cursor -> {
                            if (cursor == null)
                                return;
                            if (!cursor.moveToNext()) {
                                VideoBean videoBean = queryFormSystem(videoPath);
                                DataBaseManager.getInstance()
                                        .selectTable("file")
                                        .insert()
                                        .param("folder_path", folderPath)
                                        .param("file_path", videoBean.getVideoPath())
                                        .param("duration", String.valueOf(videoBean.getVideoDuration()))
                                        .param("file_size", String.valueOf(videoBean.getVideoSize()))
                                        .param("file_id", videoBean.get_id())
                                        .executeAsync();
                                EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_DATABASE_DATA));
                                newAddCount++;
                            }
                        });
            }
            ToastUtils.showShort("扫描完成，新增：" + newAddCount);
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void listFolder(String rootFilePath) {
        File rootFile = new File(rootFilePath);
        saveNewVideo(getVideoList(rootFile));
    }

    /**
     * 查询目录下所有视频
     */
    private List<String> getVideoList(File file) {
        List<String> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            if (fileArray == null || fileArray.length == 0) {
                return new ArrayList<>();
            } else {
                for (File childFile : fileArray) {
                    if (childFile.isDirectory()) {
                        fileList.addAll(getVideoList(childFile));
                    } else if (childFile.exists() && childFile.canRead() && CommonUtils.isMediaFile(childFile.getAbsolutePath())) {
                        fileList.add(childFile.getAbsolutePath());
                    }
                }
            }
        } else if (file.exists() && file.canRead() && CommonUtils.isMediaFile(file.getAbsolutePath())) {
            fileList.add(file.getAbsolutePath());
        }
        return fileList;
    }


    //查询系统中是否保存对应视频数据
    private VideoBean queryFormSystem(String path) {
        VideoBean videoBean = new VideoBean();
        Cursor cursor = getView().getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION},
                MediaStore.Video.Media.DATA + " = ?",
                new String[]{path}, null);
        File file = new File(path);
        videoBean.setVideoPath(path);
        videoBean.setVideoSize(file.length());
        if (cursor != null && cursor.moveToNext()) {
            videoBean.setVideoDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
            videoBean.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
            cursor.close();
        } else {
            if (cursor != null)
                cursor.close();
            videoBean.setVideoDuration(0);
            videoBean.set_id(0);
        }
        return videoBean;
    }
}
