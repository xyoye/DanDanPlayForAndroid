package com.xyoye.dandanplay.utils.jlibtorrent;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by xyoye on 2019/6/11.
 */

public class Torrent implements Parcelable{
    private String title;
    private String torrentPath;
    private String saveDirPath;
    private String animeTitle;
    private String hash;
    private String magnet;
    private long length;
    private long downloaded;
    private boolean isFinished;
    private boolean isError;
    private long downloadRate;
    private List<TorrentFile> torrentFileList;

    public Torrent() {

    }

    protected Torrent(Parcel in) {
        title = in.readString();
        torrentPath = in.readString();
        saveDirPath = in.readString();
        animeTitle = in.readString();
        hash = in.readString();
        magnet = in.readString();
        length = in.readLong();
        downloaded = in.readLong();
        isFinished = in.readInt() != 0;
        isError = in.readInt() != 0;
        downloadRate = in.readLong();
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

    public String getMagnet() {
        return magnet;
    }

    public void setMagnet(String magnet) {
        this.magnet = magnet;
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

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
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
        dest.writeString(magnet);
        dest.writeLong(length);
        dest.writeLong(downloaded);
        dest.writeInt((byte) (isFinished ? 1 : 0));
        dest.writeInt((byte) (isError ? 1 : 0));
        dest.writeLong(downloadRate);
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
