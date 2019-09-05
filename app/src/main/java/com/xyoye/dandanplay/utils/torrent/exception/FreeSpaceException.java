package com.xyoye.dandanplay.utils.torrent.exception;

/**
 * 剩余空间不足错误
 */

public class FreeSpaceException extends Exception {
    public FreeSpaceException() { }

    public FreeSpaceException(String message) {
        super(message);
    }

    public FreeSpaceException(Exception e) {
        super(e.getMessage());
        super.setStackTrace(e.getStackTrace());
    }
}
