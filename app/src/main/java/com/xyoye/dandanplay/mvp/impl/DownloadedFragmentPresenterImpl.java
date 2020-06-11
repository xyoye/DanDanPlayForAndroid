package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.database.Cursor;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.mvp.presenter.DownloadedFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadedFragmentView;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedFragmentPresenterImpl extends BaseMvpPresenterImpl<DownloadedFragmentView> implements DownloadedFragmentPresenter {
    public DownloadedFragmentPresenterImpl(DownloadedFragmentView view, LifecycleOwner lifecycleOwner) {
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
        super.destroy();
    }

    @Override
    public void queryDownloadedTask() {
        DataBaseManager.getInstance()
                .selectTable("downloaded_task")
                .query()
                .postExecute(new QueryAsyncResultCallback<List<DownloadedTaskBean>>(getLifecycle()) {
                    @Override
                    public List<DownloadedTaskBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<DownloadedTaskBean> tempList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            DownloadedTaskBean taskBean = new DownloadedTaskBean();
                            int taskId = cursor.getInt(0);
                            taskBean.set_id(taskId);
                            taskBean.setTitle(cursor.getString(1));
                            taskBean.setSaveDirPath(cursor.getString(2));
                            taskBean.setTorrentFilePath(cursor.getString(3));
                            taskBean.setTorrentHash(cursor.getString(4));
                            taskBean.setTotalSize(cursor.getLong(5));
                            taskBean.setCompleteTime(cursor.getString(6));
                            taskBean.setFileList(getTaskFileList(taskBean.getTorrentHash()));
                            tempList.add(taskBean);
                        }
                        return tempList;
                    }

                    @Override
                    public void onResult(List<DownloadedTaskBean> result) {
                        getView().updateTask(result);
                    }
                });
    }

    private List<DownloadedTaskBean.DownloadedTaskFileBean> getTaskFileList(String taskHash) {
        return DataBaseManager.getInstance()
                .selectTable("downloaded_file")
                .query()
                .where("task_torrent_hash", taskHash)
                .executeAsync(cursor -> {
                    if (cursor == null)
                        return new ArrayList<>();
                    List<DownloadedTaskBean.DownloadedTaskFileBean> fileList = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        DownloadedTaskBean.DownloadedTaskFileBean fileBean = new DownloadedTaskBean.DownloadedTaskFileBean();
                        fileBean.setFilePath(cursor.getString(2));
                        fileBean.setFileLength(cursor.getLong(3));
                        fileBean.setDanmuPath(cursor.getString(4));
                        fileBean.setEpisode_id(cursor.getInt(5));
                        fileList.add(fileBean);
                    }
                    return fileList;
                });
    }
}
