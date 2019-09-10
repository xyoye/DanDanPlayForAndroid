package com.xyoye.dandanplay.bean.params;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyoye on 2018/7/9.
 */

public class DanmuMatchParam implements Serializable{

    private String fileName;
    private String fileHash;
    private long fileSize;
    private long videoDuration;
    private String matchMode;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(String matchMode) {
        this.matchMode = matchMode;
    }

    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        map.put("fileName", this.fileName);
        map.put("fileHash", this.fileHash);
        map.put("fileSize", this.fileSize+"");
        map.put("videoDuration", this.videoDuration+"");
        map.put("matchMode", this.matchMode);
        return map;
    }
}
