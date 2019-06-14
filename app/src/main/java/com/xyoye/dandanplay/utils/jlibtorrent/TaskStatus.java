package com.xyoye.dandanplay.utils.jlibtorrent;

/**
 * Created by xyoye on 2019/6/11.
 */

public enum TaskStatus {
    STOPPED,
    PAUSED,
    ERROR,
    FINISHED,
    SEEDING,
    CHECKING,
    DOWNLOADING_METADATA,
    DOWNLOADING,
    ALLOCATING,//这个状态是猜测的，也没发现有用到
    UNKNOWN
}
