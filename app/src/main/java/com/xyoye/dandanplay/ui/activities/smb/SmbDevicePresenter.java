package com.xyoye.dandanplay.ui.activities.smb;

import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/3/30.
 */

interface SmbDevicePresenter extends BaseMvpPresenter {
    void querySqlDevice();

    void queryLanDevice();

    void addSqlDevice(SmbDeviceBean smbDeviceBean);

    void loginSmbDevice(SmbDeviceBean smbDeviceBean);

    void removeSqlDevice(String url);
}
