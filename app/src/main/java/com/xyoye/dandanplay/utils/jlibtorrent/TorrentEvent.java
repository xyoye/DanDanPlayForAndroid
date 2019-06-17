package com.xyoye.dandanplay.utils.jlibtorrent;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentEvent {
    public final static int EVENT_RESUME = 101;             //继续
    public final static int EVENT_PAUSE = 102;              //暂停
    public final static int EVENT_DELETE_TASK = 103;        //删除一个任务
    public final static int EVENT_ALL_PAUSE = 104;          //暂停所有
    public final static int EVENT_ALL_START = 105;          //开始所有
    public final static int EVENT_DELETE_ALL_TASK = 106;    //删除所有任务
    public final static int EVENT_PREPARE_PLAY = 107;       //准备播放

    private int action;
    private int position;
    //是否删除文件
    private boolean isDeleteFile;

    public TorrentEvent() {
    }

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

    public boolean isDeleteFile() {
        return isDeleteFile;
    }

    public void setDeleteFile(boolean deleteFile) {
        isDeleteFile = deleteFile;
    }
}
