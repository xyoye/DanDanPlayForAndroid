package com.xyoye.dandanplay.bean.params;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xyoye on 2019/9/10.
 */

public class BindResourceParam implements Parcelable {
    private String videoPath;
    private String searchWord;
    private int itemPosition;
    private String currentResourcePath;

    private boolean outsideFile;
    private int taskFilePosition;
    private boolean isSmbPlay;

    public BindResourceParam(String searchWord, boolean outsideFile, boolean isSmbPlay) {
        this.searchWord = searchWord;
        this.outsideFile = outsideFile;
        this.isSmbPlay = isSmbPlay;
    }

    public BindResourceParam(String videoPath, int itemPosition) {
        this.videoPath = videoPath;
        this.itemPosition = itemPosition;
    }

    public BindResourceParam(String videoPath, int itemPosition, int taskFilePosition) {
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

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getCurrentResourcePath() {
        return currentResourcePath;
    }

    public void setCurrentResourcePath(String currentResourcePath) {
        this.currentResourcePath = currentResourcePath;
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

    public boolean isSmbPlay() {
        return isSmbPlay;
    }

    public void setSmbPlay(boolean smbPlay) {
        isSmbPlay = smbPlay;
    }

    public int getTaskFilePosition() {
        return taskFilePosition;
    }

    public void setTaskFilePosition(int taskFilePosition) {
        this.taskFilePosition = taskFilePosition;
    }

    protected BindResourceParam(Parcel in) {
        videoPath = in.readString();
        searchWord = in.readString();
        itemPosition = in.readInt();
        currentResourcePath = in.readString();
        outsideFile = in.readByte() != 0;
        isSmbPlay = in.readByte() != 0;
        taskFilePosition = in.readInt();
    }

    public static final Creator<BindResourceParam> CREATOR = new Creator<BindResourceParam>() {
        @Override
        public BindResourceParam createFromParcel(Parcel in) {
            return new BindResourceParam(in);
        }

        @Override
        public BindResourceParam[] newArray(int size) {
            return new BindResourceParam[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoPath);
        dest.writeString(searchWord);
        dest.writeInt(itemPosition);
        dest.writeString(currentResourcePath);
        dest.writeByte((byte) (outsideFile ? 1 : 0));
        dest.writeByte((byte) (isSmbPlay ? 1 : 0));
        dest.writeInt(taskFilePosition);
    }
}
