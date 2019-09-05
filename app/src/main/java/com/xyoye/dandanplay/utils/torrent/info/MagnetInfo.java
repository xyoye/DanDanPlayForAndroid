package com.xyoye.dandanplay.utils.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.libtorrent4j.Priority;

import java.util.Arrays;
import java.util.List;

public class MagnetInfo implements Parcelable {
    private String uri;
    private String sha1hash;
    private String name;
    private List<Priority> filePriorities;

    public MagnetInfo(String uri, String sha1hash, String name, List<Priority> filePriorities) {
        this.uri = uri;
        this.sha1hash = sha1hash;
        this.name = name;
        this.filePriorities = filePriorities;
    }

    public MagnetInfo(String uri) throws IllegalArgumentException {
        org.libtorrent4j.AddTorrentParams p = org.libtorrent4j.AddTorrentParams.parseMagnetUri(uri);
        sha1hash = p.infoHash().toHex();
        name = (TextUtils.isEmpty(p.name()) ? sha1hash : p.name());
        filePriorities = Arrays.asList(p.filePriorities());
    }

    public MagnetInfo(Parcel s) {
        uri = s.readString();
        sha1hash = s.readString();
        name = s.readString();
        filePriorities = s.readArrayList(Priority.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeString(sha1hash);
        dest.writeString(name);
        dest.writeList(filePriorities);
    }

    public static final Creator<MagnetInfo> CREATOR =
            new Creator<MagnetInfo>() {
                @Override
                public MagnetInfo createFromParcel(Parcel source) {
                    return new MagnetInfo(source);
                }

                @Override
                public MagnetInfo[] newArray(int size) {
                    return new MagnetInfo[size];
                }
            };

    public String getUri() {
        return uri;
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

    @Override
    public int hashCode() {
        return sha1hash.hashCode();
    }

    @Override
    public String toString() {
        return "MagnetInfo{" +
                "uri='" + uri + '\'' +
                ", sha1hash='" + sha1hash + '\'' +
                ", name='" + name + '\'' +
                ", filePriorities=" + filePriorities +
                '}';
    }
}
