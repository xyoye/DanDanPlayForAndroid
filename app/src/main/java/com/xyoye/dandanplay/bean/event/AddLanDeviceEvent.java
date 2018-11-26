package com.xyoye.dandanplay.bean.event;

import com.xyoye.dandanplay.bean.LanDeviceBean;

/**
 * Created by xyy on 2018/11/22.
 */

public class AddLanDeviceEvent {
    private LanDeviceBean deviceBean;

    public AddLanDeviceEvent(LanDeviceBean deviceBean) {
        this.deviceBean = deviceBean;
    }

    public LanDeviceBean getDeviceBean() {
        return deviceBean;
    }

    public void setDeviceBean(LanDeviceBean deviceBean) {
        this.deviceBean = deviceBean;
    }
}
