package com.xyoye.dandanplay.utils.torrent.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.xyoye.dandanplay.utils.torrent.utils.AbstractStateParcel;
import com.xyoye.dandanplay.utils.torrent.utils.TorrentStateCode;

import java.util.List;

public class TaskStateBean extends AbstractStateParcel<TaskStateBean>
{
    public String torrentId = "";
    public String name = "";
    public String saveDirPath = "";
    public TorrentStateCode stateCode = TorrentStateCode.UNKNOWN;
    public int progress = 0;
    public long receivedBytes = 0L;
    public long uploadedBytes = 0L;
    public long totalBytes = 0L;
    public long downloadSpeed = 0L;
    public long uploadSpeed = 0L;
    public long ETA = -1L;
    public int totalPeers = 0;
    public int peers = 0;
    public String error;

    public List<Torrent.TorrentFile> childStateList;

    public TaskStateBean()
    {
        super();
    }

    public TaskStateBean(String torrentId, String name)
    {
        super(torrentId);

        this.torrentId = torrentId;
        this.name = name;
        this.stateCode = TorrentStateCode.STOPPED;
    }

    public TaskStateBean(String torrentId, String name,
                         String saveDirPath,
                         TorrentStateCode stateCode, int progress,
                         long receivedBytes, long uploadedBytes,
                         long totalBytes, long downloadSpeed,
                         long uploadSpeed, long ETA,
                         int totalPeers, int peers, String error, List<Torrent.TorrentFile> childStateList)
    {
        super(torrentId);

        this.torrentId = torrentId;
        this.name = name;
        this.saveDirPath = saveDirPath;
        this.stateCode = stateCode;
        this.progress = progress;
        this.receivedBytes = receivedBytes;
        this.uploadedBytes = uploadedBytes;
        this.totalBytes = totalBytes;
        this.downloadSpeed = downloadSpeed;
        this.uploadSpeed = uploadSpeed;
        this.ETA = ETA;
        this.totalPeers = totalPeers;
        this.peers = peers;
        this.error = error;
        this.childStateList = childStateList;
    }

    public TaskStateBean(Parcel source)
    {
        super(source);

        torrentId = source.readString();
        name = source.readString();
        saveDirPath = source.readString();
        stateCode = (TorrentStateCode)source.readSerializable();
        progress = source.readInt();
        receivedBytes = source.readLong();
        uploadedBytes = source.readLong();
        totalBytes = source.readLong();
        downloadSpeed = source.readLong();
        uploadSpeed = source.readLong();
        ETA = source.readLong();
        totalPeers = source.readInt();
        peers = source.readInt();
        error = source.readString();
        childStateList = source.createTypedArrayList(Torrent.TorrentFile.CREATOR);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeString(torrentId);
        dest.writeString(name);
        dest.writeString(saveDirPath);
        dest.writeSerializable(stateCode);
        dest.writeInt(progress);
        dest.writeLong(receivedBytes);
        dest.writeLong(uploadedBytes);
        dest.writeLong(totalBytes);
        dest.writeLong(downloadSpeed);
        dest.writeLong(uploadSpeed);
        dest.writeLong(ETA);
        dest.writeInt(totalPeers);
        dest.writeInt(peers);
        dest.writeString(error);
        dest.writeTypedList(childStateList);
    }

    public static final Parcelable.Creator<TaskStateBean> CREATOR =
            new Parcelable.Creator<TaskStateBean>()
            {
                @Override
                public TaskStateBean createFromParcel(Parcel source)
                {
                    return new TaskStateBean(source);
                }

                @Override
                public TaskStateBean[] newArray(int size)
                {
                    return new TaskStateBean[size];
                }
            };

    @Override
    public int compareTo(@NonNull TaskStateBean another)
    {
        return name.compareTo(another.name);
    }

    @Override
    public int hashCode()
    {
        int prime = 31, result = 1;

        result = prime * result + ((torrentId == null) ? 0 : torrentId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((saveDirPath == null) ? 0 : saveDirPath.hashCode());
        result = prime * result + ((stateCode == null) ? 0 : stateCode.hashCode());
        result = prime * result + progress;
        result = prime * result + (int) (receivedBytes ^ (receivedBytes >>> 32));
        result = prime * result + (int) (uploadedBytes ^ (uploadedBytes >>> 32));
        result = prime * result + (int) (totalBytes ^ (totalBytes >>> 32));
        result = prime * result + (int) (downloadSpeed ^ (downloadSpeed >>> 32));
        result = prime * result + (int) (uploadSpeed ^ (uploadSpeed >>> 32));
        result = prime * result + (int) (ETA ^ (ETA >>> 32));
        result = prime * result + totalPeers;
        result = prime * result + peers;
        result = prime * result + ((error == null) ? 0 : error.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TaskStateBean))
            return false;

        if (o == this)
            return true;

        TaskStateBean state = (TaskStateBean) o;

        return (torrentId == null || torrentId.equals(state.torrentId)) &&
                (name == null || name.equals(state.name)) &&
                (saveDirPath == null || saveDirPath.equals(state.saveDirPath)) &&
                (stateCode == null || stateCode.equals(state.stateCode)) &&
                progress == state.progress &&
                receivedBytes == state.receivedBytes &&
                uploadedBytes == state.uploadedBytes &&
                totalBytes == state.totalBytes &&
                downloadSpeed == state.downloadSpeed &&
                uploadSpeed == state.uploadSpeed &&
                ETA == state.ETA &&
                totalPeers == state.totalPeers &&
                peers == state.peers &&
                (error == null || error.equals(state.error));
    }

    @Override
    public String toString()
    {
        return "TaskStateBean{" +
                "torrentId='" + torrentId + '\'' +
                ", name='" + name + '\'' +
                ", saveDirPath='" + saveDirPath + '\'' +
                ", stateCode=" + stateCode +
                ", progress=" + progress +
                ", receivedBytes=" + receivedBytes +
                ", uploadedBytes=" + uploadedBytes +
                ", totalBytes=" + totalBytes +
                ", downloadSpeed=" + downloadSpeed +
                ", uploadSpeed=" + uploadSpeed +
                ", ETA=" + ETA +
                ", totalPeers=" + totalPeers +
                ", peers=" + peers +
                ", error=" + error +
                '}';
    }
}
