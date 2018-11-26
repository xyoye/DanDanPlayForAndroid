package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.LanDeviceBean;

import java.util.List;

/**
 * Created by xyy on 2018/11/19.
 */

public interface LanDevicePresenter extends BasePresenter {
    void getLanDevices();

    void searchVideo(String smbUrl);

    void authLan(LanDeviceBean deviceBean, int position, boolean isAdd);
}
