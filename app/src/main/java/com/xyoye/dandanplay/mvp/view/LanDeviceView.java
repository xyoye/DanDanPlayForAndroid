package com.xyoye.dandanplay.mvp.view;

import android.content.Context;

import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyy on 2018/11/19.
 */

public interface LanDeviceView extends BaseMvpView, LoadDataView {
    Context getContext();

    void authSuccess(LanDeviceBean deviceBean, int position);

    void searchOver();

    void addDevice(LanDeviceBean deviceBean);

    void refreshDevices(List<LanDeviceBean> devices);
}
