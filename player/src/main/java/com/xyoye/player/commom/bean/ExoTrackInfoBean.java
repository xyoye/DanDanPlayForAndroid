package com.xyoye.player.commom.bean;

/**
 * Created by XYJ on 2020/2/14.
 */

public class ExoTrackInfoBean extends TrackInfoBean {
    private int renderId;
    private int trackGroupId;
    private int trackId;

    public int getRenderId() {
        return renderId;
    }

    public void setRenderId(int renderId) {
        this.renderId = renderId;
    }

    public int getTrackGroupId() {
        return trackGroupId;
    }

    public void setTrackGroupId(int trackGroupId) {
        this.trackGroupId = trackGroupId;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }
}
