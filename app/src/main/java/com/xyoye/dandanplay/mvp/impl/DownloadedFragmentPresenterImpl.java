package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.mvp.presenter.DownloadedFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadedFragmentView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedFragmentPresenterImpl extends BaseMvpPresenterImpl<DownloadedFragmentView> implements DownloadedFragmentPresenter {
    public DownloadedFragmentPresenterImpl(DownloadedFragmentView view, Lifeful lifeful) {
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
        super.destroy();
    }

    @Override
    public void queryDownloadedTask() {
        Observable.create((ObservableOnSubscribe<List<DownloadedTaskBean>>) emitter -> {
            List<DownloadedTaskBean> taskList = DataBaseManager
                    .getInstance()
                    .selectTable("downloaded_task")
                    .query()
                    .execute(cursor -> {
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
                    });
            emitter.onNext(taskList);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DownloadedTaskBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<DownloadedTaskBean> taskList) {
                        getView().updateTask(taskList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    private List<DownloadedTaskBean.DownloadedTaskFileBean> getTaskFileList(String taskHash) {
        return DataBaseManager.getInstance()
                .selectTable("downloaded_file")
                .query()
                .where("task_torrent_hash", taskHash)
                .execute(cursor -> {
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
