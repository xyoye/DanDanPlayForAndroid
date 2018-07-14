package com.xyoye.dandanplay.event;

import com.xyoye.dandanplay.bean.DanmuMatchBean;

import java.io.Serializable;

/**
 * Created by YE on 2018/7/14.
 */


public class DownloadDanmuEvent implements Serializable{
    private DanmuMatchBean.MatchesBean model;

    public DownloadDanmuEvent(DanmuMatchBean.MatchesBean model) {
        this.model = model;
    }

    public DanmuMatchBean.MatchesBean getModel() {
        return model;
    }

    public void setModel(DanmuMatchBean.MatchesBean model) {
        this.model = model;
    }
}
