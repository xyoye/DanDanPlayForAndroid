package com.xyoye.player.ijkplayer.media;

/**
 * Created by xyy on 2018/9/30.
 */

public class VideoInfoTrack {
    private int stream;
    private String name;
    private boolean isSelect;
    private String language;

    public VideoInfoTrack() {
    }

    public VideoInfoTrack(int stream, String name, boolean isSelect) {
        this.stream = stream;
        this.name = name;
        this.isSelect = isSelect;
    }

    public int getStream() {
        return stream;
    }

    public void setStream(int stream) {
        this.stream = stream;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
