package com.xyoye.dandanplay.event;

import com.xyoye.dandanplay.bean.VideoBean;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/1.
 */


public class OpenVideoEvent implements Serializable {
    private VideoBean bean;
    private int position;

    public OpenVideoEvent(VideoBean bean, int position) {
        this.bean = bean;
        this.position = position;
    }

    public VideoBean getBean() {
        return bean;
    }

    public void setBean(VideoBean bean) {
        this.bean = bean;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
