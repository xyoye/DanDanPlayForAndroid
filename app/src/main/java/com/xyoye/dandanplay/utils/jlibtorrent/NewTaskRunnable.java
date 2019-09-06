package com.xyoye.dandanplay.utils.jlibtorrent;

import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.xyoye.dandanplay.utils.Constants;

import java.io.File;

/**
 * 开始新的下载任务
 */

public class NewTaskRunnable implements Runnable {
    private Torrent mTorrent;
    private TorrentEngine engine;
    private OnNewTaskCallBack callBack;

    public NewTaskRunnable(Torrent torrent, TorrentEngine engine) {
        this.mTorrent = torrent;
        this.engine = engine;
        this.callBack = engine;
    }

    @Override
    public void run() {
        try {
            File torrentFile = new File(mTorrent.getTorrentPath());
            File saveDirFile = new File(mTorrent.getSaveDirPath());
            File resumeFile = new File(Constants.DefaultConfig.torrentResumeFilePath);

            TorrentInfo torrentInfo = new TorrentInfo(torrentFile);
            mTorrent.setHash(torrentInfo.infoHash().toHex());

            Priority[] priorities = mTorrent.getPriorities();
            if (priorities == null || priorities.length != torrentInfo.numFiles()) {
                return;
            }

            if(callBack.beforeAddTask(mTorrent))
                engine.download(torrentInfo, saveDirFile, resumeFile, mTorrent.getPriorities(), null);
        } catch (Exception ignore) {
        }

    }

    public interface OnNewTaskCallBack {
        boolean beforeAddTask(Torrent containsHashTorrent);
    }
}