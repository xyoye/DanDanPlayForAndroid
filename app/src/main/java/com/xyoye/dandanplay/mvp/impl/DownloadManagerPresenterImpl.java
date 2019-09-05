package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskInfo;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentUtil;

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

public class DownloadManagerPresenterImpl extends BaseMvpPresenterImpl<DownloadManagerView> implements DownloadManagerPresenter {

    private Disposable serviceDis = null;

    public DownloadManagerPresenterImpl(DownloadManagerView view, Lifeful lifeful) {
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
        if (serviceDis != null)
            serviceDis.dispose();
    }

    @Override
    public void observeService() {
        //等待服务开启后增加新任务
        getView().showLoading();
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int waitTime = 0;
            while (true) {
                try {
                    if (ServiceUtils.isServiceRunning(TorrentService.class)) {
                        e.onNext(true);
                        break;
                    }
                    if (waitTime > 10) {
                        e.onError(new RuntimeException("开启下载服务失败"));
                        break;
                    }
                    waitTime++;
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        getView().hideLoading();
                        //启动服务后恢复任务
                        if (IApplication.isFirstOpenTaskPage) {
                            IApplication.isFirstOpenTaskPage = false;
                            recoveryTask();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().hideLoading();
                        getView().showError("开启下载服务失败");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 恢复任务
     */
    private void recoveryTask() {
        Observable.create((ObservableOnSubscribe<Torrent>) e -> {
            //如果任务列表不为空，不恢复任务
            if (TaskInfo.taskList.size() > 0)
                return;

            //查询已保存的任务
            Cursor cursor = TorrentUtil.queryRecoveryTorrent();
            while (cursor.moveToNext()) {
                Torrent torrent = new Torrent();
                torrent.setTorrentPath(cursor.getString(3));
                torrent.setAnimeTitle(cursor.getString(5));
                String prioritiesSaveData = cursor.getString(6);
                String[] priorities;
                if (prioritiesSaveData.contains(";")) {
                    priorities = prioritiesSaveData.split(";");
                } else {
                    priorities = new String[]{prioritiesSaveData};
                }

                //根据种子文件读取torrent信息
                TorrentInfo torrentInfo = TorrentUtil.getTorrentInfoForFile(torrent.getTorrentPath());
                if (torrentInfo == null)
                    continue;
                if (priorities.length != torrentInfo.numFiles())
                    continue;
                if (TaskInfo.taskMap.containsKey(torrentInfo.infoHash().toString()))
                    continue;

                String saveDirPath = AppConfig.getInstance().getDownloadFolder() +
                        ((StringUtils.isEmpty(torrent.getAnimeTitle()))
                                ? ("/" + torrentInfo.name())
                                : ("/" + torrent.getAnimeTitle()));

                torrent.setSaveDirPath(saveDirPath);
                torrent.setHash(torrentInfo.infoHash().toString());
                torrent.setTitle(torrentInfo.name());
                torrent.setLength(torrentInfo.totalSize());
                List<Torrent.TorrentFile> torrentFileList = new ArrayList<>();

                for (int i = 0; i < priorities.length; i++) {
                    Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
                    torrentFile.setName(torrentInfo.files().fileName(i));
                    torrentFile.setPath(saveDirPath + "/" + torrentInfo.files().filePath(i));
                    torrentFile.setLength(torrentInfo.files().fileSize(i));
                    torrentFile.setChecked("1".equals(priorities[i]));
                    torrentFileList.add(torrentFile);
                }
                torrent.setTorrentFileList(torrentFileList);
                e.onNext(torrent);
            }
            e.onComplete();
            getView().hideLoading();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Torrent>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Torrent torrent) {
                        new TorrentTask(torrent, true).startTask();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        getView().startNewTask();
                    }
                });
    }
}
