package com.xyoye.dandanplay.utils.torrent;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.AppConfigShare;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import libtorrent.Libtorrent;
import libtorrent.StatsTorrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentTask{

    private Context context;

    public TorrentTask(Context context){
        this.context = context;
    }

    public Torrent prepare(Torrent torrent){
        return prepareTorrent(torrent);
    }

    public boolean start(Torrent torrent){
        return startTorrent(torrent);
    }

    public void pause(Torrent torrent){
        pauseTorrent(torrent);
    }

    //解析torrent内容
    private Torrent prepareTorrent(Torrent oldTorrent){
        String downloadFolder = AppConfigShare.getInstance().getDownloadFolder();
        downloadFolder += oldTorrent.getFolder();
        Torrent torrent = new Torrent();
        torrent.setPath(oldTorrent.getPath());
        if (oldTorrent.getFolder().endsWith("/"))
            torrent.setFolder(downloadFolder);
        else
            torrent.setFolder(downloadFolder+"/");
        torrent.setEpisodeId(oldTorrent.getEpisodeId());
        torrent.setDanmuPath(oldTorrent.getDanmuPath());
        torrent.setDone(oldTorrent.isDone());

        byte[] torrentData;
        File torrentFile = new File(oldTorrent.getPath());
        try {
            torrentData = FileUtils.readFileToByteArray(torrentFile);
        } catch (IOException e) {
            ToastUtils.showShort("找不到种子文件，目前暂不支持读取SD卡文件");
            throw new RuntimeException(e);
        }
        File folder = new File(downloadFolder);
        if (!folder.exists()){
            if (!folder.mkdirs()){
                throw new RuntimeException("not found download folder, create folder fail ："+downloadFolder);
            }
        }
        Uri uri = Uri.fromFile(folder);

        if (!TorrentUtil.prepareTorrentFromBytes(torrent, uri, torrentData)){
            Toast.makeText(context, "解析种子文件失败", Toast.LENGTH_LONG).show();
            return null;
        }else {
            return torrent;
        }
    }

    //下载torrent内容
    private boolean startTorrent(Torrent torrent) {
        int torrentStatus = Libtorrent.torrentStatus(torrent.getId());
        torrent.setStatus(torrentStatus);
        if (torrentStatus == Libtorrent.StatusPaused || torrentStatus == Libtorrent.StatusQueued){
            if (!Libtorrent.startTorrent(torrent.getId())){
                TLog.e(Libtorrent.error());
                Toast.makeText(context, "错误，无法下载", Toast.LENGTH_LONG).show();
                return false;
            }
            if (Libtorrent.torrentTrackersCount(torrent.getId()) == 0){
                for (String tracker : IApplication.trackers){
                    Libtorrent.torrentTrackerAdd(torrent.getId(), tracker);
                }
            }
            StatsTorrent b = Libtorrent.torrentStats(torrent.getId());
            torrent.downloaded.start(b.getDownloaded());
            torrent.uploaded.start(b.getUploaded());
        }
        return true;
    }

    //暂停torrent下载
    private void pauseTorrent(Torrent torrent){
        if (torrent.getId() == -1)
            return;
        Libtorrent.stopTorrent(torrent.getId());
        IApplication.updateTorrent(torrent);
        StatsTorrent b = Libtorrent.torrentStats(torrent.getId());
        torrent.downloaded.end(b.getDownloaded());
        torrent.uploaded.end(b.getUploaded());
    }
}
