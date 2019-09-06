package com.xyoye.dandanplay.utils.jlibtorrent;

public enum TorrentStateCode {
    //未知
    UNKNOWN(-1),
    //错误
    ERROR(0),
    //播种中
    SEEDING(1),
    //下载中
    DOWNLOADING(2),
    //已暂停
    PAUSED(3),
    //已停止
    STOPPED(4),
    //正在检查文件，尚未开始下载
    CHECKING(5),
    //下载完成（一些无法下载的碎片已跳过）
    FINISHED(6),
    //分配磁盘中
    ALLOCATING(7);

    private final int value;

    TorrentStateCode(int value) {
        this.value = value;
    }

    public static TorrentStateCode fromValue(int value) {
        for (TorrentStateCode ev : TorrentStateCode.class.getEnumConstants())
            if (ev.value() == value)
                return ev;

        return UNKNOWN;
    }

    public int value() {
        return value;
    }
}