package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.DownloadedFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadedFragmentView;
import com.xyoye.dandanplay.utils.Lifeful;

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
            List<DownloadedTaskBean> taskList = new ArrayList<>();
            Cursor taskCursor = DataBaseManager
                    .getInstance()
                    .selectTable(14)
                    .query()
                    .execute();
            while (taskCursor.moveToNext()) {
                DownloadedTaskBean taskBean = new DownloadedTaskBean();
                int taskId = taskCursor.getInt(0);
                taskBean.set_id(taskId);
                taskBean.setTitle(taskCursor.getString(1));
                taskBean.setSaveDirPath(taskCursor.getString(2));
                taskBean.setTorrentFilePath(taskCursor.getString(3));
                taskBean.setTorrentHash(taskCursor.getString(4));
                taskBean.setTotalSize(taskCursor.getLong(5));
                taskBean.setCompleteTime(taskCursor.getString(6));
                taskBean.setFileList(getTaskFileList(taskBean.getTorrentHash()));
                taskList.add(taskBean);
            }

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
        List<DownloadedTaskBean.DownloadedTaskFileBean> fileList = new ArrayList<>();
        Cursor fileCursor = DataBaseManager
                .getInstance()
                .selectTable(15)
                .query()
                .where(1, taskHash)
                .execute();
        while (fileCursor.moveToNext()) {
            DownloadedTaskBean.DownloadedTaskFileBean fileBean = new DownloadedTaskBean.DownloadedTaskFileBean();
            fileBean.setFilePath(fileCursor.getString(2));
            fileBean.setFileLength(fileCursor.getLong(3));
            fileBean.setDanmuPath(fileCursor.getString(4));
            fileBean.setEpisode_id(fileCursor.getInt(5));
            fileList.add(fileBean);
        }

        return fileList;
    }
}
