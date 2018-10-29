package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2018/9/29.
 */

public class PlayerSettingEvent {
    private boolean isPlayer;
    private String name;

    public PlayerSettingEvent(boolean isPlayer, String name) {
        this.isPlayer = isPlayer;
        this.name = name;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
