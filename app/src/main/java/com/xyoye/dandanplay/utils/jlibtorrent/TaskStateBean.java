package com.xyoye.dandanplay.utils.jlibtorrent;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskStateBean implements Parcelable, Comparable<TaskStateBean> {
    private String torrentHash;
    private String taskName;
    private String saveDirPath;
    private TorrentStateCode stateCode;
    private int progress;
    private long receivedBytes;
    private long totalBytes;
    private long downloadSpeed;
    private long taskBuildTime;

    public static TaskStateBean buildTaskState(TorrentTask torrentTask){
        Torrent torrent = torrentTask.getTorrent();
        return new TaskStateBean(
                torrent.getHash(),
                torrent.getTitle(),
                torrent.getSaveDirPath(),
                torrentTask.getStateCode(),
                torrentTask.getProgress(),
                torrentTask.getTotalReceivedBytes(),
                torrentTask.getTotalWanted(),
                torrentTask.getDownloadSpeed(),
                torrent.getTaskBuildTime()
        );
    }

    private TaskStateBean(String torrentHash,
                         String name,
                         String saveDirPath,
                         TorrentStateCode stateCode,
                         int progress,
                         long receivedBytes,
                         long totalBytes,
                         long downloadSpeed,
                         long taskBuildTime) {

        this.torrentHash = torrentHash;
        this.taskName = name;
        this.saveDirPath = saveDirPath;
        this.stateCode = stateCode;
        this.progress = progress;
        this.receivedBytes = receivedBytes;
        this.totalBytes = totalBytes;
        this.downloadSpeed = downloadSpeed;
        this.taskBuildTime = taskBuildTime;
    }

    public TaskStateBean(Parcel source) {
        torrentHash = source.readString();
        taskName = source.readString();
        saveDirPath = source.readString();
        stateCode = (TorrentStateCode) source.readSerializable();
        progress = source.readInt();
        receivedBytes = source.readLong();
        totalBytes = source.readLong();
        downloadSpeed = source.readLong();
        taskBuildTime = source.readLong();
    }

    public String getTorrentHash() {
        return torrentHash;
    }

    public void setTorrentHash(String torrentHash) {
        this.torrentHash = torrentHash;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public TorrentStateCode getStateCode() {
        return stateCode;
    }

    public void setStateCode(TorrentStateCode stateCode) {
        this.stateCode = stateCode;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(torrentHash);
        dest.writeString(taskName);
        dest.writeString(saveDirPath);
        dest.writeSerializable(stateCode);
        dest.writeInt(progress);
        dest.writeLong(receivedBytes);
        dest.writeLong(totalBytes);
        dest.writeLong(downloadSpeed);
        dest.writeLong(taskBuildTime);
    }

    public static final Parcelable.Creator<TaskStateBean> CREATOR =
            new Parcelable.Creator<TaskStateBean>() {
                @Override
                public TaskStateBean createFromParcel(Parcel source) {
                    return new TaskStateBean(source);
                }

                @Override
                public TaskStateBean[] newArray(int size) {
                    return new TaskStateBean[size];
                }
            };

    @Override
    public int hashCode() {
        int prime = 31, result = 1;

        result = prime * result + ((torrentHash == null) ? 0 : torrentHash.hashCode());
        result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
        result = prime * result + ((saveDirPath == null) ? 0 : saveDirPath.hashCode());
        result = prime * result + ((stateCode == null) ? 0 : stateCode.hashCode());
        result = prime * result + progress;
        result = prime * result + (int) (receivedBytes ^ (receivedBytes >>> 32));
        result = prime * result + (int) (totalBytes ^ (totalBytes >>> 32));
        result = prime * result + (int) (downloadSpeed ^ (downloadSpeed >>> 32));
        result = prime * result + (int) (taskBuildTime ^ (taskBuildTime >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TaskStateBean))
            return false;

        if (o == this)
            return true;

        TaskStateBean state = (TaskStateBean) o;

        return (torrentHash == null || torrentHash.equals(state.torrentHash)) &&
                (taskName == null || taskName.equals(state.taskName)) &&
                (saveDirPath == null || saveDirPath.equals(state.saveDirPath)) &&
                (stateCode == null || stateCode.equals(state.stateCode)) &&
                progress == state.progress &&
                receivedBytes == state.receivedBytes &&
                totalBytes == state.totalBytes &&
                downloadSpeed == state.downloadSpeed &&
                taskBuildTime == state.taskBuildTime;
    }

    @Override
    public int compareTo(TaskStateBean o) {
        return Long.compare(taskBuildTime, o.taskBuildTime);
    }
}