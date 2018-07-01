package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.utils.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderPresenterImpl extends BaseMvpPresenter<FolderView> implements FolderPresenter {

    public FolderPresenterImpl(FolderView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        refreshVideos();
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
    public void refreshVideos() {
        String folderPath = getView().getFolderPath();
        List<VideoBean> videoBeans = getVideoList(folderPath);
        getView().refreshAdapter(videoBeans);
    }

    private List<VideoBean> getVideoList(String folderPath){
        List<VideoBean> videoBeans = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM file WHERE folder_path = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql ,new String[]{folderPath});
        while (cursor.moveToNext()){
            String fileName = cursor.getString(2);
            String filePath = cursor.getString(1) + fileName;
            videoBeans.add(getVideoInfoMore(new VideoBean(fileName, filePath)));
        }
        cursor.close();
        return videoBeans;
    }

    private VideoBean getVideoInfoMore(VideoBean videoBean){
        FFmpegMediaMetadataRetriever fmmr = null;
        String videoCover = "";
        String videoDuration = "";
        try {
            fmmr = new FFmpegMediaMetadataRetriever();
            fmmr.setDataSource(videoBean.getVideoPath());
            Bitmap bitmap = fmmr.getFrameAtTime();
            videoCover = BitmapUtil.bitmapToBase64(bitmap);
            videoDuration = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }finally {
            if (fmmr != null){
                fmmr.release();
            }
        }

        videoBean.setVideoCover(videoCover);
        videoBean.setVideoDuration(videoDuration);
        return videoBean;
    }
}
