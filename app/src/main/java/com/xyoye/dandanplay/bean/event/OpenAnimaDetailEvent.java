package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/20.
 */


public class OpenAnimaDetailEvent implements Serializable {
    private String animaId;

    public OpenAnimaDetailEvent(String animaId) {
        this.animaId = animaId;
    }

    public String getAnimaId() {
        return animaId;
    }

    public void setAnimaId(String animaId) {
        this.animaId = animaId;
    }
}
