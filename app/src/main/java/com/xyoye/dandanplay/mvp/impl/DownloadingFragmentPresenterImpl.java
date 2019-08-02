package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.DownloadingFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadingFragmentView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;

import java.util.List;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadingFragmentPresenterImpl extends BaseMvpPresenterImpl<DownloadingFragmentView> implements DownloadingFragmentPresenter {

    public DownloadingFragmentPresenterImpl(DownloadingFragmentView view, Lifeful lifeful) {
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
    public void setTaskFinish(BtTask task) {
        //保存任务到已完成
        Torrent torrent = task.getTorrent();
        long taskId = DataBaseManager.getInstance()
                .selectTable(14)
                .insert()
                .param(1, torrent.getTitle())
                .param(2, torrent.getSaveDirPath())
                .param(3, torrent.getMagnet())
                .param(4, CommonUtils.convertFileSize(torrent.getLength()))
                .param(5, torrent.getHash())
                .execute();
        List<Torrent.TorrentFile> torrentFileList = torrent.getTorrentFileList();
        for (Torrent.TorrentFile torrentFile : torrentFileList){
            DataBaseManager.getInstance()
                    .selectTable(15)
                    .insert()
                    .param(1, taskId)
                    .param(2, torrentFile.getPath())
                    .param(3, torrentFile.getDanmuPath())
                    .param(4, torrentFile.getEpisodeId())
                    .postExecute();
        }
        //从下载中任务删除
        DataBaseManager.getInstance()
                .selectTable(6)
                .delete()
                .where(1, torrent.getTorrentPath())
                .execute();
    }
}
