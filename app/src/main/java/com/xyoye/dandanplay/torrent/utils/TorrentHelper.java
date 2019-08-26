package com.xyoye.dandanplay.torrent.utils;

import android.text.TextUtils;

import com.xyoye.dandanplay.torrent.TorrentEngine;
import com.xyoye.dandanplay.torrent.TorrentTask;
import com.xyoye.dandanplay.torrent.info.TaskStateBean;
import com.xyoye.dandanplay.torrent.info.Torrent;
import com.xyoye.dandanplay.torrent.info.TorrentMetaInfo;

import org.libtorrent4j.Priority;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xyoye on 2019/8/22.
 */

public class TorrentHelper {

    /**
     * 创建任务
     */
    public static synchronized void addTorrent(Torrent torrent) throws Exception {
        if (TextUtils.isEmpty(torrent.getTorrentFilePath())) {
            throw new IllegalArgumentException("种子文件地址为空");
        } else if (!(new File(torrent.getTorrentFilePath()).exists())) {
            throw new FileNotFoundException("找不到种子文件");
        }

        TorrentMetaInfo torrentMetaInfo = new TorrentMetaInfo(torrent.getTorrentFilePath());

        Priority[] priorities = TorrentUtils.getPriorities(torrent);
        if (priorities.length == 0) {
            torrent.setPriorities(Collections.nCopies(torrentMetaInfo.fileCount, Priority.DEFAULT));
        }

        List<Torrent.TorrentFile> childFileList = new ArrayList<>();
        for (TorrentMetaInfo.TorrentMetaFileInfo fileInfo : torrentMetaInfo.fileList){
            Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
            torrentFile.setFilePath(fileInfo.getPath());
            torrentFile.setFileLength(fileInfo.getSize());
            torrentFile.setChecked(true);
            childFileList.add(fileInfo.getIndex(), torrentFile);
        }
        torrent.setChildFileList(childFileList);

        TorrentEngine.getInstance().download(torrent);
    }


    /**
     * 创建任务状态类
     */
    public static TaskStateBean buildTaskState(TorrentTask task) {
        if (task == null)
            return null;

        Torrent torrent = task.getTorrent();
        long[] progress = task.getChildFileProgress();
        if (torrent.getChildFileList().size() == progress.length){
            for (int i = 0; i < torrent.getChildFileList().size(); i++) {
                Torrent.TorrentFile torrentFile = torrent.getChildFileList().get(i);
                torrentFile.setFileDoneLength(progress[i]);
            }
        }

        return new TaskStateBean(
                torrent.getTorrentHash(),
                torrent.getTaskName(),
                task.getStateCode(),
                task.getProgress(),
                task.getTotalReceivedBytes(),
                task.getTotalSentBytes(),
                task.getTotalWanted(),
                task.getDownloadSpeed(),
                task.getUploadSpeed(),
                task.getETA(),
                task.getTotalPeers(),
                task.getConnectedPeers(),
                torrent.getErrorMsg(),
                torrent.getChildFileList());
    }
}
