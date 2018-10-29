package com.xyoye.dandanplay.utils.torrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentEvent {
    public final static int EVENT_START = 100;
    public final static int EVENT_RESUME = 101;
    public final static int EVENT_PAUSE = 102;
    public final static int EVENT_STOP = 103;

    private int action;
    private Torrent torrent;

    public TorrentEvent() {
    }

    public TorrentEvent(int action, Torrent torrent) {
        this.action = action;
        this.torrent = torrent;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }
}
