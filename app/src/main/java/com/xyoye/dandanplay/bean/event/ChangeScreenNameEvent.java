package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by xyoye on 2019/5/27.
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
