package com.xyoye.dandanplay.utils.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;

import org.libtorrent4j.Priority;

import java.util.List;

public class AddTorrentParams implements Parcelable {
    //torrent文件路径获取magnet链接
    private String source;
    private boolean fromMagnet;
    private String sha1hash;
    private String name;
    private List<Priority> filePriorities;
    private String pathToDownload;
    private boolean sequentialDownload;
    private boolean addPaused;

    public AddTorrentParams(String source, boolean fromMagnet, String sha1hash,
                            String name, List<Priority> filePriorities, String pathToDownload,
                            boolean sequentialDownload, boolean addPaused) {
        this.source = source;
        this.fromMagnet = fromMagnet;
        this.sha1hash = sha1hash;
        this.name = name;
        this.filePriorities = filePriorities;
        this.pathToDownload = pathToDownload;
        this.sequentialDownload = sequentialDownload;
        this.addPaused = addPaused;
    }

    public AddTorrentParams(Parcel s) {
        source = s.readString();
        fromMagnet = s.readByte() != 0;
        sha1hash = s.readString();
        name = s.readString();
        filePriorities = s.readArrayList(Priority.class.getClassLoader());
        pathToDownload = s.readString();
        sequentialDownload = s.readByte() != 0;
        addPaused = s.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeByte((byte) (fromMagnet ? 1 : 0));
        dest.writeString(sha1hash);
        dest.writeString(name);
        dest.writeList(filePriorities);
        dest.writeString(pathToDownload);
        dest.writeByte((byte) (sequentialDownload ? 1 : 0));
        dest.writeByte((byte) (addPaused ? 1 : 0));
    }

    public static final Creator<AddTorrentParams> CREATOR =
            new Creator<AddTorrentParams>() {
                @Override
                public AddTorrentParams createFromParcel(Parcel source) {
                    return new AddTorrentParams(source);
                }

                @Override
                public AddTorrentParams[] newArray(int size) {
                    return new AddTorrentParams[size];
                }
            };

    public String getSource() {
        return source;
    }

    public boolean fromMagnet() {
        return fromMagnet;
    }

    public String getSha1hash() {
        return sha1hash;
    }

    public String getName() {
        return name;
    }

    public List<Priority> getFilePriorities() {
        return filePriorities;
    }

    public String getPathToDownload() {
        return pathToDownload;
    }

    public boolean isSequentialDownload() {
        return sequentialDownload;
    }

    public boolean addPaused() {
        return addPaused;
    }

    @Override
    public int hashCode() {
        return sha1hash.hashCode();
    }

    @Override
    public String toString() {
        return "AddTorrentParams{" +
                "source='" + source + '\'' +
                ", fromMagnet=" + fromMagnet +
                ", sha1hash='" + sha1hash + '\'' +
                ", name='" + name + '\'' +
                ", filePriorities=" + filePriorities +
                ", pathToDownload='" + pathToDownload + '\'' +
                ", sequentialDownload=" + sequentialDownload +
                ", addPaused=" + addPaused +
                '}';
    }
}
