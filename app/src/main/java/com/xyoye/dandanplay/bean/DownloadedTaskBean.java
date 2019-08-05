package com.xyoye.dandanplay.bean;

import java.util.List;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedTaskBean {
    private int _id;
    private String title;
    private String folderPath;
    private String magnet;
    private String totalSize;
    private String torrentHash;
    private String completeTime;
    private List<DownloadedTaskFileBean> fileList;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getMagnet() {
        return magnet;
    }

    public void setMagnet(String magnet) {
        this.magnet = magnet;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getTorrentHash() {
        return torrentHash;
    }

    public void setTorrentHash(String torrentHash) {
        this.torrentHash = torrentHash;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public List<DownloadedTaskFileBean> getFileList() {
        return fileList;
    }

    public void setFileList(List<DownloadedTaskFileBean> fileList) {
        this.fileList = fileList;
    }

    public static class DownloadedTaskFileBean {
        private String filePath;
        private long fileLength;
        private String danmuPath;
        private int episode_id;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileLength() {
            return fileLength;
        }

        public void setFileLength(long fileLength) {
            this.fileLength = fileLength;
        }

        public String getDanmuPath() {
            return danmuPath;
        }

        public void setDanmuPath(String danmuPath) {
            this.danmuPath = danmuPath;
        }

        public int getEpisode_id() {
            return episode_id;
        }

        public void setEpisode_id(int episode_id) {
            this.episode_id = episode_id;
        }
    }
}
