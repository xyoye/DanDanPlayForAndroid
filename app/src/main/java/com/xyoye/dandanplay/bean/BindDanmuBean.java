package com.xyoye.dandanplay.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/9/10.
 */

public class BindDanmuBean implements Parcelable {
    private String danmuPath;
    private int episodeId;
    private int itemPosition;

    private int taskFilePosition;

    public BindDanmuBean() {
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

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public int getTaskFilePosition() {
        return taskFilePosition;
    }

    public void setTaskFilePosition(int taskFilePosition) {
        this.taskFilePosition = taskFilePosition;
    }

    protected BindDanmuBean(Parcel in) {
        danmuPath = in.readString();
        episodeId = in.readInt();
        itemPosition = in.readInt();
        taskFilePosition = in.readInt();
    }

    public static final Creator<BindDanmuBean> CREATOR = new Creator<BindDanmuBean>() {
        @Override
        public BindDanmuBean createFromParcel(Parcel in) {
            return new BindDanmuBean(in);
        }

        @Override
        public BindDanmuBean[] newArray(int size) {
            return new BindDanmuBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(danmuPath);
        dest.writeInt(episodeId);
        dest.writeInt(itemPosition);
        dest.writeInt(taskFilePosition);
    }
}
