package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.ScanFolderBean;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.VideoScanFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.VideoScanFragmentView;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/5/14.
 */

public class VideoScanFragmentPresenterImpl extends BaseMvpPresenterImpl<VideoScanFragmentView> implements VideoScanFragmentPresenter {


    public VideoScanFragmentPresenterImpl(VideoScanFragmentView view, Lifeful lifeful) {
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
    public void addScanFolder(String path, boolean isScan) {
        String scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
        DataBaseManager.getInstance()
                .selectTable(11)
                .insert()
                .param(1, path)
                .param(2, scanType)
                .execute();

        queryScanFolderList(isScan);
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_SYSTEM_DATA));
    }

    @Override
    public void queryScanFolderList(boolean isScan) {
        String scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
                Cursor cursor = DataBaseManager.getInstance()
                .selectTable(11)
                .query()
                .where(2, scanType)
                .execute();

        List<ScanFolderBean> folderList = new ArrayList<>();
        while (cursor.moveToNext()) {
            folderList.add(new ScanFolderBean(cursor.getString(1), false));
        }
        cursor.close();
        getView().updateFolderList(folderList);
    }

    @Override
    public void deleteScanFolder(String path, boolean isScan) {
        String scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
        if (Constants.DefaultConfig.SYSTEM_VIDEO_PATH.equals(path)){
            //将不删除系统视频，只改变为屏蔽或扫描
            String newScanType = isScan ? Constants.ScanType.BLOCK : Constants.ScanType.SCAN;
            DataBaseManager.getInstance()
                    .selectTable(11)
                    .update()
                    .where(1, path)
                    .where(2, scanType)
                    .param(2, newScanType)
                    .execute();
        }else {
            DataBaseManager.getInstance()
                    .selectTable(11)
                    .delete()
                    .where(1, path)
                    .where(2, scanType)
                    .execute();
        }
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_SYSTEM_DATA));
    }
}
