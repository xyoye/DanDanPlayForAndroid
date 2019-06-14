package com.xyoye.dandanplay.bean.event;

import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;

/**
 * Created by xyoye on 2019/6/12.
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
