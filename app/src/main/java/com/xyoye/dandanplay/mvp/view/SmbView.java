package com.xyoye.dandanplay.mvp.view;

import android.content.Context;

import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/3/30.
 */

public interface SmbView extends BaseMvpView, LoadDataView {
    void refreshSqlDevice(List<SmbBean> deviceList);

    void refreshLanDevice(List<SmbBean> deviceList);

    void refreshSmbFile(List<SmbBean> deviceList, String parentPath);

    Context getContext();
}
