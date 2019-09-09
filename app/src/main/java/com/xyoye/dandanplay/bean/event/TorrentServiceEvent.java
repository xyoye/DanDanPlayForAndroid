package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2019/9/9.
 */

public class TorrentServiceEvent {
    private boolean isTaskFinish;

    public TorrentServiceEvent() {

    }

    public TorrentServiceEvent(boolean isTaskFinish) {
        this.isTaskFinish = isTaskFinish;
    }

    public boolean isTaskFinish() {
        return isTaskFinish;
    }
}
