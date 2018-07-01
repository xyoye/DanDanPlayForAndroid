package com.xyoye.dandanplay.event;

import com.xyoye.dandanplay.bean.VideoBean;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/1.
 */


public class OpenVideoEvent implements Serializable {
    private VideoBean bean;

    public OpenVideoEvent(VideoBean bean) {
        this.bean = bean;
    }

    public VideoBean getBean() {
        return bean;
    }

    public void setBean(VideoBean bean) {
        this.bean = bean;
    }
}
