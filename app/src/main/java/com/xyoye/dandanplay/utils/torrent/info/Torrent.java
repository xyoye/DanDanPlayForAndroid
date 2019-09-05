package com.xyoye.dandanplay.utils.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.xyoye.dandanplay.utils.torrent.utils.TorrentUtils;

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
    private List<TorrentFile> childFileList;

    /**
     * 新增下载任务时使用
     */
    public Torrent(String animeTitle, String torrentFilePath, String saveDirPath, List<Priority> priorityList) {
        this.animeTitle = animeTitle;
        this.torrentFilePath = torrentFilePath;
        this.saveDirPath = saveDirPath;
        this.priorities = priorityList;
    }

    /**
     * 恢复任务时用
     */
    public Torrent(String animeTitle, String torrentFilePath, String saveDirPath) {
        this.animeTitle = animeTitle;
        this.torrentFilePath = torrentFilePath;
        this.saveDirPath = saveDirPath;
    }

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
        childFileList = in.createTypedArrayList(TorrentFile.CREATOR);
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
        for (Object priority : priorities) {
            priorityArrayList.add((Priority) priority);
        }
        return priorityArrayList;
    }

    public String getPriorityStr() {
        if (priorities.size() == 0)
            return "";
        StringBuilder prioritiesStr = new StringBuilder();
        for (Object priorityObj : priorities) {
            Priority priority = (Priority) priorityObj;
            prioritiesStr.append(priority.swig()).append(";");
        }
        return prioritiesStr.substring(0, prioritiesStr.length() - 1);
    }

    public void setPriorityStr(String priorityStr) {
        if (TextUtils.isEmpty(priorityStr)) {
            this.priorities = new ArrayList();
        } else {
            if (priorityStr.contains(";")) {
                List<Priority> priorityList = new ArrayList<>();
                for (String str : priorityStr.split(";")) {
                    Priority priority;
                    try {
                        int value = Integer.valueOf(str);
                        priority = Priority.fromSwig(value);
                    } catch (Exception e) {
                        priority = Priority.DEFAULT;
                    }
                    priorityList.add(priority);
                }
                this.priorities = priorityList;
            } else {
                Priority priority;
                try {
                    int value = Integer.valueOf(priorityStr);
                    priority = Priority.fromSwig(value);
                } catch (Exception e) {
                    priority = Priority.DEFAULT;
                }
                List<Priority> priorityList = new ArrayList<>();

                priorityList.add(priority);
                this.priorities = priorityList;
            }
        }
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public String getMagnetLink() {
        return TorrentUtils.MAGNET_HEADER + torrentHash;
    }

    public List<TorrentFile> getChildFileList() {
        return childFileList == null ? new ArrayList<>() : childFileList;
    }

    public void setChildFileList(List<TorrentFile> childFileList) {
        this.childFileList = childFileList;
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
        dest.writeTypedList(childFileList);
    }

    public static class TorrentFile implements Parcelable {
        private String filePath;
        private long fileLength;
        private long fileDoneLength;
        private String danmuFilePath;
        private int danmuEpisodeId;
        private boolean isChecked;

        public TorrentFile() {
        }

        protected TorrentFile(Parcel in) {
            filePath = in.readString();
            fileLength = in.readLong();
            fileDoneLength = in.readLong();
            danmuFilePath = in.readString();
            danmuEpisodeId = in.readInt();
            isChecked = in.readByte() != 0;
        }

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

        public long getFileDoneLength() {
            return fileDoneLength;
        }

        public void setFileDoneLength(long fileDoneLength) {
            this.fileDoneLength = fileDoneLength;
        }

        public String getDanmuFilePath() {
            return danmuFilePath;
        }

        public void setDanmuFilePath(String danmuFilePath) {
            this.danmuFilePath = danmuFilePath;
        }

        public int getDanmuEpisodeId() {
            return danmuEpisodeId;
        }

        public void setDanmuEpisodeId(int danmuEpisodeId) {
            this.danmuEpisodeId = danmuEpisodeId;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public static Creator<TorrentFile> getCREATOR() {
            return CREATOR;
        }

        public static final Creator<TorrentFile> CREATOR = new Creator<TorrentFile>() {
            @Override
            public TorrentFile createFromParcel(Parcel in) {
                return new TorrentFile(in);
            }

            @Override
            public TorrentFile[] newArray(int size) {
                return new TorrentFile[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(filePath);
            dest.writeLong(fileLength);
            dest.writeLong(fileDoneLength);
            dest.writeString(danmuFilePath);
            dest.writeInt(danmuEpisodeId);
            dest.writeByte((byte) (isChecked ? 1 : 0));
        }
    }
}
