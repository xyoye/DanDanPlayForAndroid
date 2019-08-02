package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
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
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/10/27.
 */

public class DownloadManagerPresenterImpl extends BaseMvpPresenterImpl<DownloadManagerView> implements DownloadManagerPresenter {
    private Disposable serviceDis = null;
    private  Disposable taskDis = null;

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
        if (taskDis != null)
            taskDis.dispose();
    }

    @Override
    public void getTorrentList() {

    }

    public void observeService() {
        //等待服务开启后增加新任务
        getView().showLoading("正在开启下载服务");
        serviceDis = io.reactivex.Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int waitTime = 0;
            while (true){
                try {
                    if(ServiceUtils.isServiceRunning(TorrentService.class)){
                        getView().hideLoading();
                        e.onNext(true);
                        e.onComplete();
                        break;
                    }
                    if (waitTime > 10){
                        getView().hideLoading();
                        getView().showError("开启下载服务失败");
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
                .subscribe(aBoolean ->
                        getView().startNewTask());
    }

    @Override
    public void recoveryTask() {
        getView().showLoading();
        taskDis = io.reactivex.Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            //如果任务列表不为空，不恢复任务
            if (IApplication.taskList.size() > 0)
                return;
            //查询已保存的任务
            Cursor cursor = TorrentUtil.queryDBTorrent();
            while (cursor.moveToNext()){
                Torrent torrent = new Torrent();
                torrent.setTorrentPath(cursor.getString(1));
                torrent.setAnimeTitle(cursor.getString(2));
                torrent.setMagnet(cursor.getString(3));
                torrent.setFinished(cursor.getInt(4) == 1);
                String prioritiesSaveData = cursor.getString(5);
                String[] priorities;
                if (prioritiesSaveData.contains(";")){
                    priorities = prioritiesSaveData.split(";");
                }else {
                    priorities = new String[]{prioritiesSaveData};
                }

                //根据路径读取torrent信息
                TorrentInfo torrentInfo = TorrentUtil.getTorrentInfoForFile(torrent.getTorrentPath());
                if (torrentInfo == null)
                    continue;
                if (priorities.length != torrentInfo.numFiles())
                    continue;
                if (IApplication.taskMap.containsKey(torrentInfo.infoHash().toString()))
                    continue;

                String saveDirPath = AppConfig.getInstance().getDownloadFolder() +
                        ((StringUtils.isEmpty(torrent.getAnimeTitle()))
                                ? ("/"+torrentInfo.name())
                                : ("/"+torrent.getAnimeTitle()));
                torrent.setSaveDirPath(saveDirPath);
                torrent.setHash(torrentInfo.infoHash().toString());
                torrent.setTitle(torrentInfo.name());
                torrent.setLength(torrentInfo.totalSize());
                List<Torrent.TorrentFile> torrentFileList = new ArrayList<>();

                for (int i=0; i<priorities.length; i++){
                    Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
                    torrentFile.setName(torrentInfo.files().fileName(i));
                    torrentFile.setPath(saveDirPath + "/" +torrentInfo.files().filePath(i));
                    torrentFile.setLength(torrentInfo.files().fileSize(i));
                    torrentFile.setChecked("1".equals(priorities[i]));
                    torrentFileList.add(torrentFile);
                }
                torrent.setTorrentFileList(torrentFileList);

                //启动任务
                BtTask btTask = new BtTask(torrent);
                btTask.startTask(true);
            }
            getView().hideLoading();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(Boolean -> LogUtils.e("recover task over"));
    }
}
