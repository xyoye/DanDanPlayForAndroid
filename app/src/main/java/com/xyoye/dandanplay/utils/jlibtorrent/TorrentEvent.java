package com.xyoye.dandanplay.utils.jlibtorrent;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentEvent {
    public final static int EVENT_RESUME = 101;             //继续
    public final static int EVENT_PAUSE = 102;              //暂停
    public final static int EVENT_DELETE_FILE = 104;        //删除一个任务
    public final static int EVENT_DELETE_TASK = 105;        //删除一个任务和文件
    public final static int EVENT_ALL_PAUSE = 106;          //暂停所有
    public final static int EVENT_ALL_START = 107;          //开始所有
    public final static int EVENT_ALL_DELETE_FILE = 108;    //删除所有任务和文件
    public final static int EVENT_ALL_DELETE_TASK = 109;    //删除所以任务
    public final static int EVENT_CLOSE_PLAY = 110;         //播放任务结束

    private int action;
    private int position;

    public TorrentEvent(int action, int position) {
        this.action = action;
        this.position = position;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
