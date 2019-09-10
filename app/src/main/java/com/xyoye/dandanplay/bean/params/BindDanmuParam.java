package com.xyoye.dandanplay.bean.params;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/9/10.
 */

public class BindDanmuParam implements Parcelable {
    private String videoPath;
    private int itemPosition;

    private boolean outsideFile;
    private int taskFilePosition;

    public BindDanmuParam(String videoPath, boolean outsideFile) {
        this.videoPath = videoPath;
        this.outsideFile = outsideFile;
    }

    public BindDanmuParam(String videoPath, int itemPosition) {
        this.videoPath = videoPath;
        this.itemPosition = itemPosition;
    }

    public BindDanmuParam(String videoPath, int itemPosition, int taskFilePosition) {
        this.videoPath = videoPath;
        this.itemPosition = itemPosition;
        this.taskFilePosition = taskFilePosition;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public boolean isOutsideFile() {
        return outsideFile;
    }

    public void setOutsideFile(boolean outsideFile) {
        this.outsideFile = outsideFile;
    }

    public int getTaskFilePosition() {
        return taskFilePosition;
    }

    public void setTaskFilePosition(int taskFilePosition) {
        this.taskFilePosition = taskFilePosition;
    }

    protected BindDanmuParam(Parcel in) {
        videoPath = in.readString();
        itemPosition = in.readInt();
        outsideFile = in.readByte() != 0;
        taskFilePosition = in.readInt();
    }

    public static final Creator<BindDanmuParam> CREATOR = new Creator<BindDanmuParam>() {
        @Override
        public BindDanmuParam createFromParcel(Parcel in) {
            return new BindDanmuParam(in);
        }

        @Override
        public BindDanmuParam[] newArray(int size) {
            return new BindDanmuParam[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoPath);
        dest.writeInt(itemPosition);
        dest.writeByte((byte) (outsideFile ? 1 : 0));
        dest.writeInt(taskFilePosition);
    }
}
