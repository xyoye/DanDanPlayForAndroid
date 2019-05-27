package com.xyoye.dandanplay.mvp.view;

import android.content.Context;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by xyoye on 2018/8/5.
 */

public interface RegisterView extends BaseMvpView, LoadDataView {
    Context getRegisterContext();

    void registerSuccess();
}
