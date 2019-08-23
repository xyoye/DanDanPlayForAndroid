package com.xyoye.dandanplay.torrent.exception;

/**
 * torrent解析错误
 */

public class TorrentDecodeException extends Exception {

    public TorrentDecodeException(Exception e) {
        super(e.getMessage());
        super.setStackTrace(e.getStackTrace());
    }
}
