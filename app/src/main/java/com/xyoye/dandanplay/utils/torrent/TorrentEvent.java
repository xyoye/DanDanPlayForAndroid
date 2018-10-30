package com.xyoye.dandanplay.utils.torrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentEvent {
    public final static int EVENT_START = 100;              //开始
    public final static int EVENT_RESUME = 101;             //继续
    public final static int EVENT_PAUSE = 102;              //暂停
    public final static int EVENT_STOP = 103;               //停止
    public final static int EVENT_DELETE_FILE = 104;        //删除一个任务
    public final static int EVENT_DELETE_TASK = 105;        //删除一个任务和文件
    public final static int EVENT_ALL_PAUSE = 106;          //暂停所有
    public final static int EVENT_ALL_START = 107;          //开始所有
    public final static int EVENT_ALL_DELETE_FILE = 108;    //删除所有任务和文件
    public final static int EVENT_ALL_DELETE_TASK = 109;    //删除所以任务

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
