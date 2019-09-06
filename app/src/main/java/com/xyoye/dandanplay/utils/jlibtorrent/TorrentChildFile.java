package com.xyoye.dandanplay.utils.jlibtorrent;

/**
 * Created by xyoye on 2019/9/6.
 */

public class TorrentChildFile {
    private String fileName;
    private long fileSize;
    private long fileReceived;
    private boolean isChecked;

    public TorrentChildFile(String fileName, long fileSize, long fileReceived, boolean isChecked) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileReceived = fileReceived;
        this.isChecked = isChecked;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileReceived() {
        return fileReceived;
    }

    public void setFileReceived(long fileReceived) {
        this.fileReceived = fileReceived;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
