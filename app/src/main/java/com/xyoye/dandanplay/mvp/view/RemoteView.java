package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.RemoteVideoBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/7/11.
 */

public interface RemoteView extends BaseMvpView, LoadDataView {
    void refreshVideoList(List<RemoteVideoBean> remoteVideoBeanList);

    void onDanmuBind(String hash, String danmuPath);
}
