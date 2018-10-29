package com.xyoye.dandanplay.utils.torrent;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.xyoye.dandanplay.utils.AppConfigShare;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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

    public void start(Torrent torrent){
        startTorrent(torrent);
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
        torrent.setFolder(downloadFolder);

        byte[] torrentData;
        File torrentFile = new File(oldTorrent.getPath());
        try {
            torrentData = FileUtils.readFileToByteArray(torrentFile);
        } catch (IOException e) {
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
    private void startTorrent(Torrent torrent) {
        int torrentStatus = Libtorrent.torrentStatus(torrent.getId());
        torrent.setStatus(torrentStatus);
        if (torrentStatus == Libtorrent.StatusPaused || torrentStatus == Libtorrent.StatusQueued){
            if (!Libtorrent.startTorrent(torrent.getId()))
                throw new RuntimeException(Libtorrent.error());
            StatsTorrent b = Libtorrent.torrentStats(torrent.getId());
            torrent.downloaded.start(b.getDownloaded());
            torrent.uploaded.start(b.getUploaded());
        }else{
            Libtorrent.stopTorrent(torrent.getId());
        }
    }

    //暂停torrent下载
    private void pauseTorrent(Torrent torrent){
        if (torrent.getId() == -1)
            return;
        Libtorrent.stopTorrent(torrent.getId());
        StatsTorrent b = Libtorrent.torrentStats(torrent.getId());
        torrent.downloaded.end(b.getDownloaded());
        torrent.uploaded.end(b.getUploaded());
    }
}
