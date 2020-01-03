package com.xyoye.dandanplay.ui.activities.smb;

import android.content.Context;

import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/3/30.
 */

interface SmbDeviceView extends BaseMvpView, LoadDataView {
    void refreshSqlDevice(List<SmbDeviceBean> deviceList);

    void refreshLanDevice(List<SmbDeviceBean> deviceList);

    void loginSuccess();

    Context getContext();
}
