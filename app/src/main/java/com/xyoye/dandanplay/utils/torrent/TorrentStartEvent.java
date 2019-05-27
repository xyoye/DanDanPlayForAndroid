package com.xyoye.dandanplay.utils.torrent;

/**
 * Created by xyoye on 2019/3/21.
 */

public class TorrentStartEvent {
    private Torrent torrent;

    public TorrentStartEvent(Torrent torrent) {
        this.torrent = torrent;
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }
}
