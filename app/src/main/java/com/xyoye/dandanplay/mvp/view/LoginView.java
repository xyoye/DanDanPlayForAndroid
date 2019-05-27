package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by xyoye on 2018/7/22.
 */

public interface LoginView extends BaseMvpView, LoadDataView {
    void launchMain();
}
