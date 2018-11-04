package com.xyoye.dandanplay.mvp.impl;

import android.Manifest;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.utils.SearchDanmuUtil;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void updateDanmu(String danmuPath, int episodeId, String[] whereArgs) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String whereCase = DataBaseInfo.getFieldNames()[2][1]+" =? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
        ContentValues values = new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][3],danmuPath);
        values.put(DataBaseInfo.getFieldNames()[2][6],episodeId);
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[2],values,whereCase,whereArgs);
    }

    @Override
    public void updateCurrent(SaveCurrentEvent event) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String whereCase = DataBaseInfo.getFieldNames()[2][1]+" =? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
        ContentValues values = new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][4], event.getCurrentPosition());
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[2], values, whereCase, new String[]{event.getFolderPath(), event.getVideoName()});
    }

    @Override
    public void deleteFile(String folderPath, String fileName) {
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String whereCase = DataBaseInfo.getFieldNames()[2][1]+" =? AND "+ DataBaseInfo.getFieldNames()[2][2]+" =? ";
        sqLiteDatabase.delete(DataBaseInfo.getTableNames()[2], whereCase, new String[]{folderPath, fileName});
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
    }


    private List<VideoBean> getVideoList(String folderPath){
        List<VideoBean> videoBeans = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();

        String sql = "SELECT * FROM file WHERE folder_path = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql ,new String[]{folderPath});
        while (cursor.moveToNext()){
            String fileName = cursor.getString(2);
            String filePath = folderPath + fileName;

            File file = new File(filePath);
            if (!file.exists()){
                sqLiteDatabase.delete("file", "folder_path = ? AND file_name = ?", new String[]{folderPath, fileName});
                continue;
            }

            String danmuPath = cursor.getString(3);
            int currentPosition = cursor.getInt(4);
            long duration = Long.parseLong(cursor.getString(5));
            int episodeId = cursor.getInt(6);
            videoBeans.add(new VideoBean(fileName, filePath, danmuPath, currentPosition, duration, episodeId));
        }
        cursor.close();
        return videoBeans;
    }

    @Override
    public void getDanmu(String videoPath){
        String title = FileUtils.getFileName(videoPath);
        String hash = SearchDanmuUtil.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = SearchDanmuUtil.getVideoDuration(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
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
}
