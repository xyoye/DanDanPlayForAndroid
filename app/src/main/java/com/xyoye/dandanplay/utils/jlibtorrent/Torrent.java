package com.xyoye.dandanplay.utils.jlibtorrent;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.frostwire.jlibtorrent.Priority;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/6/11.
 */

public class Torrent implements Parcelable {
    private String title;
    private String torrentPath;
    private String saveDirPath;
    private String hash;
    private boolean isRestoreTask;
    private long taskBuildTime;
    private List<Priority> priorityList;

    /**
     * 新增任务
     */
    public Torrent(String torrentFilePath, String saveDirPath, List<Priority> priorityList) {
        this.torrentPath = torrentFilePath;
        this.saveDirPath = saveDirPath;
        this.priorityList = priorityList;
    }

    /**
     * 恢复任务
     */
    public Torrent(String torrentFilePath, String saveDirPath, String priorityStr) {
        this.torrentPath = torrentFilePath;
        this.saveDirPath = saveDirPath;
        this.priorityList = priority2List(priorityStr);
    }

    protected Torrent(Parcel in) {
        title = in.readString();
        torrentPath = in.readString();
        saveDirPath = in.readString();
        hash = in.readString();
        isRestoreTask = in.readInt() != 0;
        taskBuildTime = in.readLong();
        priorityList = priority2List(in.readString());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTorrentPath() {
        return torrentPath;
    }

    public void setTorrentPath(String path) {
        this.torrentPath = path;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isRestoreTask() {
        return isRestoreTask;
    }

    public void setRestoreTask(boolean recoveryTask) {
        isRestoreTask = recoveryTask;
    }

    public long getTaskBuildTime() {
        return taskBuildTime;
    }

    public void setTaskBuildTime(long taskBuildTime) {
        this.taskBuildTime = taskBuildTime;
    }

    public Priority[] getPriorities() {
        if (priorityList == null || priorityList.size() == 0) {
            return new Priority[0];
        }

        return priorityList.toArray(new Priority[0]);
    }

    public String getPriorityStr() {
        if (priorityList == null || priorityList.size() == 0) {
            return "";
        }

        StringBuilder priorityBuilder = new StringBuilder();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityBuilder.append(priorityList.get(i).swig()).append(";");
        }

        return priorityBuilder.substring(0, priorityBuilder.length() - 1);
    }

    public List<Priority> priority2List(String priorityStr) {
        List<Priority> priorityList = new ArrayList<>();
        if (priorityStr.contains(";")) {
            for (String str : priorityStr.split(";")) {
                int value = Integer.valueOf(str);
                priorityList.add(Priority.fromSwig(value));
            }
        } else {
            int value = Integer.valueOf(priorityStr);
            priorityList.add(Priority.fromSwig(value));
        }
        return priorityList;
    }

    /**
     * 是否能生成任务
     */
    public boolean isCanBeTask() {
        return !TextUtils.isEmpty(torrentPath) &&
                !TextUtils.isEmpty(saveDirPath) &&
                priorityList.size() > 0;
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
        dest.writeString(title);
        dest.writeString(torrentPath);
        dest.writeString(saveDirPath);
        dest.writeString(hash);
        dest.writeInt((byte) (isRestoreTask ? 1 : 0));
        dest.writeLong(taskBuildTime);
        dest.writeString(getPriorityStr());
    }
}
