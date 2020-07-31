package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.ScanFolderBean;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.mvp.presenter.VideoScanFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.VideoScanFragmentView;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/5/14.
 */

public class VideoScanFragmentPresenterImpl extends BaseMvpPresenterImpl<VideoScanFragmentView> implements VideoScanFragmentPresenter {


    public VideoScanFragmentPresenterImpl(VideoScanFragmentView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
        int scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .insert()
                .param("folder_path", path)
                .param("folder_type", scanType)
                .postExecute();

        queryScanFolderList(isScan);
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_SYSTEM_DATA));
    }

    @Override
    public void queryScanFolderList(boolean isScan) {
        int scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .query()
                .where("folder_type", String.valueOf(scanType))
                .postExecute(new QueryAsyncResultCallback<List<ScanFolderBean>>(getLifecycle()) {
                    @Override
                    public List<ScanFolderBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<ScanFolderBean> folderList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            folderList.add(new ScanFolderBean(cursor.getString(1), false));
                        }
                        return folderList;
                    }

                    @Override
                    public void onResult(List<ScanFolderBean> result) {
                        getView().updateFolderList(result);
                    }
                });


    }

    @Override
    public void deleteScanFolder(String path, boolean isScan) {
        int scanType = isScan ? Constants.ScanType.SCAN : Constants.ScanType.BLOCK;
        if (Constants.DefaultConfig.SYSTEM_VIDEO_PATH.equals(path)) {
            //将不删除系统视频，只改变为屏蔽或扫描
            int newScanType = isScan ? Constants.ScanType.BLOCK : Constants.ScanType.SCAN;
            DataBaseManager.getInstance()
                    .selectTable("scan_folder")
                    .update()
                    .where("folder_path", path)
                    .where("folder_type", String.valueOf(scanType))
                    .param("folder_type", newScanType)
                    .postExecute();
        } else {
            DataBaseManager.getInstance()
                    .selectTable("scan_folder")
                    .delete()
                    .where("folder_path", path)
                    .where("folder_type", String.valueOf(scanType))
                    .postExecute();
        }
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_SYSTEM_DATA));
    }
}
