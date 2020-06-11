package com.xyoye.dandanplay.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/9/10.
 */

public class BindResourceBean implements Parcelable {
    private String danmuPath;
    private String zimuPath;
    private int episodeId;
    private int itemPosition;

    private int taskFilePosition;

    public BindResourceBean() {
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public String getZimuPath() {
        return zimuPath;
    }

    public void setZimuPath(String zimuPath) {
        this.zimuPath = zimuPath;
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

    protected BindResourceBean(Parcel in) {
        danmuPath = in.readString();
        zimuPath = in.readString();
        episodeId = in.readInt();
        itemPosition = in.readInt();
        taskFilePosition = in.readInt();
    }

    public static final Creator<BindResourceBean> CREATOR = new Creator<BindResourceBean>() {
        @Override
        public BindResourceBean createFromParcel(Parcel in) {
            return new BindResourceBean(in);
        }

        @Override
        public BindResourceBean[] newArray(int size) {
            return new BindResourceBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(danmuPath);
        dest.writeString(zimuPath);
        dest.writeInt(episodeId);
        dest.writeInt(itemPosition);
        dest.writeInt(taskFilePosition);
    }
}
