package com.xyoye.dandanplay.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;

import com.xyoye.dandanplay.torrent.utils.TorrentUtils;

import org.libtorrent4j.Priority;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/8/23.
 */

public class Torrent implements Parcelable {
    private String taskName;
    private String torrentFilePath;
    private String torrentHash;
    private String saveDirPath;
    private String animeTitle;

    private String errorMsg;
    private boolean finished;
    private boolean error;
    private boolean paused;
    private boolean sequentialDownload;

    private List priorities;

    protected Torrent(Parcel in) {
        taskName = in.readString();
        torrentFilePath = in.readString();
        saveDirPath = in.readString();
        animeTitle = in.readString();
        torrentHash = in.readString();
        errorMsg = in.readString();
        finished = in.readByte() != 0;
        error = in.readByte() != 0;
        paused = in.readByte() != 0;
        sequentialDownload = in.readByte() != 0;
        priorities = in.readArrayList(Priority.class.getClassLoader());
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTorrentFilePath() {
        return torrentFilePath;
    }

    public void setTorrentFilePath(String torrentFilePath) {
        this.torrentFilePath = torrentFilePath;
    }

    public String getTorrentHash() {
        return torrentHash;
    }

    public void setTorrentHash(String torrentHash) {
        this.torrentHash = torrentHash;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isSequentialDownload() {
        return sequentialDownload;
    }

    public void setSequentialDownload(boolean sequentialDownload) {
        this.sequentialDownload = sequentialDownload;
    }

    public List<Priority> getPriorities() {
        List<Priority> priorityArrayList = new ArrayList<>();
        for (Object priority : priorities){
            priorityArrayList.add((Priority)priority);
        }
        return priorityArrayList;
    }

    public String getPrioritiesStr(){
        if (priorities.size() == 0)
            return "";
        StringBuilder prioritiesStr = new StringBuilder();
        for (Object priorityObj : priorities){
            Priority priority = (Priority)priorityObj;
            prioritiesStr.append(priority.swig()).append(";");
        }
        return prioritiesStr.substring(0, prioritiesStr.length()-1);
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public String getMagnetLink(){
        return TorrentUtils.MAGNET_HEADER + torrentHash;
    }

    public static final Creator<Torrent> CREATOR = new Creator<Torrent>() {
        @Override
        public Torrent createFromParcel(Parcel in) {
            return new Torrent(in);
        }

        @Override
        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskName);
        dest.writeString(torrentFilePath);
        dest.writeString(saveDirPath);
        dest.writeString(animeTitle);
        dest.writeString(torrentHash);
        dest.writeString(errorMsg);
        dest.writeByte((byte) (finished ? 1 : 0));
        dest.writeByte((byte) (error ? 1 : 0));
        dest.writeByte((byte) (paused ? 1 : 0));
        dest.writeByte((byte) (sequentialDownload ? 1 : 0));
        dest.writeList(priorities);
    }
}
