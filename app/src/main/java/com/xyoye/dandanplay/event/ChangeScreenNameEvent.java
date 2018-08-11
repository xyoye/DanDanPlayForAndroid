package com.xyoye.dandanplay.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/8/11.
 */


public class ChangeScreenNameEvent implements Serializable {
    private String screenName;

    public ChangeScreenNameEvent(String screenName) {
        this.screenName = screenName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
