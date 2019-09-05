package com.xyoye.dandanplay.utils.jlibtorrent;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostwire.jlibtorrent.Priority;

import java.util.List;

/**
 * Created by xyoye on 2019/6/11.
 */

public class Torrent implements Parcelable {
    private String title;
    private String torrentPath;
    private String saveDirPath;
    private String animeTitle;
    private String hash;
    private long length;
    private long downloaded;
    private boolean isError;
    private long downloadRate;
    private boolean isRecoveryTask;
    private List<TorrentFile> torrentFileList;

    public Torrent() {

    }

    protected Torrent(Parcel in) {
        title = in.readString();
        torrentPath = in.readString();
        saveDirPath = in.readString();
        animeTitle = in.readString();
        hash = in.readString();
        length = in.readLong();
        downloaded = in.readLong();
        isError = in.readInt() != 0;
        downloadRate = in.readLong();
        isRecoveryTask = in.readInt() != 0;
        torrentFileList = in.createTypedArrayList(TorrentFile.CREATOR);
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

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public long getDownloadRate() {
        return downloadRate;
    }

    public void setDownloadRate(long downloadRate) {
        this.downloadRate = downloadRate;
    }

    public boolean isRecoveryTask() {
        return isRecoveryTask;
    }

    public void setRecoveryTask(boolean recoveryTask) {
        isRecoveryTask = recoveryTask;
    }

    public Priority[] getPriporities() {
        if (torrentFileList == null || torrentFileList.size() == 0) {
            return new Priority[0];
        }

        Priority[] priorities = new Priority[torrentFileList.size()];
        for (int i = 0; i < torrentFileList.size(); i++) {
            priorities[i] = torrentFileList.get(i).isChecked()
                    ? Priority.NORMAL
                    : Priority.IGNORE;
        }

        return priorities;
    }

    public String getPriorityStr() {
        if (torrentFileList == null || torrentFileList.size() == 0) {
            return "";
        }

        StringBuilder priorityBuilder = new StringBuilder();
        for (int i = 0; i < torrentFileList.size(); i++) {
            priorityBuilder.append(
                    (torrentFileList.get(i).isChecked())
                            ? ("1;")
                            : ("0;")
            );
        }

        return priorityBuilder.substring(0, priorityBuilder.length() - 1);
    }

    public List<TorrentFile> getTorrentFileList() {
        return torrentFileList;
    }

    public void setTorrentFileList(List<TorrentFile> torrentFileList) {
        this.torrentFileList = torrentFileList;
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
        dest.writeString(animeTitle);
        dest.writeString(hash);
        dest.writeLong(length);
        dest.writeLong(downloaded);
        dest.writeInt((byte) (isError ? 1 : 0));
        dest.writeLong(downloadRate);
        dest.writeInt((byte) (isRecoveryTask ? 1 : 0));
        dest.writeTypedList(torrentFileList);
    }

    public static class TorrentFile implements Parcelable {
        private boolean isChecked;
        private long length;
        private String path;
        private String name;
        private String danmuPath;
        private int episodeId;
        private long downloaded;

        public TorrentFile() {
        }

        protected TorrentFile(Parcel in) {
            isChecked = in.readByte() != 0;
            path = in.readString();
            name = in.readString();
            danmuPath = in.readString();
            episodeId = in.readInt();
            length = in.readLong();
            downloaded = in.readLong();
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDanmuPath() {
            return danmuPath;
        }

        public void setDanmuPath(String danmuPath) {
            this.danmuPath = danmuPath;
        }

        public int getEpisodeId() {
            return episodeId;
        }

        public void setEpisodeId(int episodeId) {
            this.episodeId = episodeId;
        }

        public long getDownloaded() {
            return downloaded;
        }

        public void setDownloaded(long downloaded) {
            this.downloaded = downloaded;
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
            dest.writeByte((byte) (isChecked ? 1 : 0));
            dest.writeString(path);
            dest.writeString(name);
            dest.writeString(danmuPath);
            dest.writeInt(episodeId);
            dest.writeLong(length);
            dest.writeLong(downloaded);
        }
    }
}
