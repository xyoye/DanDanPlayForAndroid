package com.xyoye.dandanplay.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.xyoye.dandanplay.torrent.exception.TorrentDecodeException;

import org.libtorrent4j.FileStorage;
import org.libtorrent4j.TorrentInfo;

import java.io.File;
import java.util.ArrayList;

public class TorrentMetaInfo implements Parcelable {
    public String torrentName = "";
    public String sha1Hash = "";
    public String comment = "";
    public String createdBy = "";
    public long torrentSize = 0L;
    public long creationDate = 0L;
    public int fileCount = 0;
    public int pieceLength = 0;
    public int numPieces = 0;
    public ArrayList<TorrentMetaFileInfo> fileList = new ArrayList<>();

    public TorrentMetaInfo(String pathToTorrent) throws TorrentDecodeException {
        File torrentFile = new File(pathToTorrent);
        try {
            getMetaInfo(new TorrentInfo(torrentFile));

        } catch (Exception e) {
            throw new TorrentDecodeException(e);
        }
    }

    public TorrentMetaInfo(String torrentName, String sha1hash) {
        this.torrentName = torrentName;
        this.sha1Hash = sha1hash;
    }

    public TorrentMetaInfo(byte[] data) throws TorrentDecodeException {
        try {
            getMetaInfo(TorrentInfo.bdecode(data));

        } catch (Exception e) {
            throw new TorrentDecodeException(e);
        }
    }

    public TorrentMetaInfo(TorrentInfo info) throws TorrentDecodeException {
        try {
            getMetaInfo(info);

        } catch (Exception e) {
            throw new TorrentDecodeException(e);
        }
    }

    private void getMetaInfo(TorrentInfo info) {
        torrentName = info.name();
        sha1Hash = info.infoHash().toHex();
        comment = info.comment();
        createdBy = info.creator();
        //转换Unix时间
        creationDate = info.creationDate() * 1000L;
        torrentSize = info.totalSize();
        fileCount = info.numFiles();
        fileList = getFileList(info.origFiles());
        pieceLength = info.pieceLength();
        numPieces = info.numPieces();
    }

    public TorrentMetaInfo(Parcel source) {
        torrentName = source.readString();
        sha1Hash = source.readString();
        comment = source.readString();
        createdBy = source.readString();
        torrentSize = source.readLong();
        creationDate = source.readLong();
        fileCount = source.readInt();
        fileList = new ArrayList<>();
        source.readTypedList(fileList, TorrentMetaFileInfo.CREATOR);
        pieceLength = source.readInt();
        numPieces = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(torrentName);
        dest.writeString(sha1Hash);
        dest.writeString(comment);
        dest.writeString(createdBy);
        dest.writeLong(torrentSize);
        dest.writeLong(creationDate);
        dest.writeInt(fileCount);
        dest.writeTypedList(fileList);
        dest.writeInt(pieceLength);
        dest.writeInt(numPieces);
    }

    public static final Creator<TorrentMetaInfo> CREATOR =
            new Creator<TorrentMetaInfo>() {
                @Override
                public TorrentMetaInfo createFromParcel(Parcel source) {
                    return new TorrentMetaInfo(source);
                }

                @Override
                public TorrentMetaInfo[] newArray(int size) {
                    return new TorrentMetaInfo[size];
                }
            };

    @Override
    public int hashCode() {
        return sha1Hash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TorrentMetaInfo))
            return false;

        if (o == this)
            return true;

        TorrentMetaInfo info = (TorrentMetaInfo) o;

        return (torrentName == null || torrentName.equals(info.torrentName)) &&
                (sha1Hash == null || sha1Hash.equals(info.sha1Hash)) &&
                (comment == null || comment.equals(info.comment)) &&
                (createdBy == null || createdBy.equals(info.createdBy)) &&
                torrentSize == info.torrentSize &&
                creationDate == info.creationDate &&
                fileCount == info.fileCount &&
                pieceLength == info.pieceLength &&
                numPieces == info.numPieces;
    }

    @Override
    public String toString() {
        return "TorrentMetaInfo{" +
                "torrentName='" + torrentName + '\'' +
                ", sha1Hash='" + sha1Hash + '\'' +
                ", comment='" + comment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", torrentSize=" + torrentSize +
                ", creationDate=" + creationDate +
                ", fileCount=" + fileCount +
                ", pieceLength=" + pieceLength +
                ", numPieces=" + numPieces +
                ", fileList=" + fileList +
                '}';
    }

    public static class TorrentMetaFileInfo implements Parcelable, Comparable<TorrentMetaFileInfo> {
        private String path;
        private int index;
        private long size;

        public TorrentMetaFileInfo(String path, int index, long size) {
            this.path = path;
            this.index = index;
            this.size = size;
        }

        public TorrentMetaFileInfo(Parcel source) {
            path = source.readString();
            index = source.readInt();
            size = source.readLong();
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(path);
            dest.writeInt(index);
            dest.writeLong(size);
        }

        static final Creator<TorrentMetaFileInfo> CREATOR =
                new Creator<TorrentMetaFileInfo>() {
                    @Override
                    public TorrentMetaFileInfo createFromParcel(Parcel source) {
                        return new TorrentMetaFileInfo(source);
                    }

                    @Override
                    public TorrentMetaFileInfo[] newArray(int size) {
                        return new TorrentMetaFileInfo[size];
                    }
                };

        @Override
        public int compareTo(@NonNull TorrentMetaFileInfo anotner) {
            return path.compareTo(anotner.path);
        }

        @Override
        public String toString() {
            return "TorrentMetaFileInfo{" +
                    "path='" + path + '\'' +
                    ", index=" + index +
                    ", size=" + size +
                    '}';
        }
    }

    public static ArrayList<TorrentMetaFileInfo> getFileList(FileStorage storage) {
        ArrayList<TorrentMetaFileInfo> files = new ArrayList<>();
        for (int i = 0; i < storage.numFiles(); i++) {
            TorrentMetaFileInfo file = new TorrentMetaFileInfo(storage.filePath(i), i, storage.fileSize(i));
            files.add(file);
        }

        return files;
    }
}
