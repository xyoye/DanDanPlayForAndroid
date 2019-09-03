package com.xyoye.dandanplay.bean.params;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/9/3.
 */

public class PlayParam implements Parcelable {
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private long currentPosition;
    private int episodeId;

    private int sourceOrigin;
    private long thunderTaskId;

    public PlayParam() {
    }

    protected PlayParam(Parcel in) {
        videoPath = in.readString();
        videoTitle = in.readString();
        danmuPath = in.readString();
        currentPosition = in.readLong();
        episodeId = in.readInt();
        sourceOrigin = in.readInt();
        thunderTaskId = in.readLong();
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public int getSourceOrigin() {
        return sourceOrigin;
    }

    public void setSourceOrigin(int sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    public long getThunderTaskId() {
        return thunderTaskId;
    }

    public void setThunderTaskId(long thunderTaskId) {
        this.thunderTaskId = thunderTaskId;
    }

    public static Creator<PlayParam> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<PlayParam> CREATOR = new Creator<PlayParam>() {
        @Override
        public PlayParam createFromParcel(Parcel in) {
            return new PlayParam(in);
        }

        @Override
        public PlayParam[] newArray(int size) {
            return new PlayParam[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoPath);
        dest.writeString(videoTitle);
        dest.writeString(danmuPath);
        dest.writeLong(currentPosition);
        dest.writeInt(episodeId);
        dest.writeInt(sourceOrigin);
        dest.writeLong(thunderTaskId);
    }
}
