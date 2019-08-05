package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskBindDanmuEndEvent {
    private int taskPosition;
    private int taskFilePosition;
    private String danmuPath;
    private int episodeId;

    public int getTaskPosition() {
        return taskPosition;
    }

    public void setTaskPosition(int taskPosition) {
        this.taskPosition = taskPosition;
    }

    public int getTaskFilePosition() {
        return taskFilePosition;
    }

    public void setTaskFilePosition(int taskFilePosition) {
        this.taskFilePosition = taskFilePosition;
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
}
