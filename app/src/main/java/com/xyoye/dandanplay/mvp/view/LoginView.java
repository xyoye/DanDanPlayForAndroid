package com.xyoye.dandanplay.mvp.view;

import android.app.Activity;
import android.content.Context;

import com.xyoye.core.interf.view.BaseMvpView;

/**
 * Created by YE on 2018/7/22.
 */


public interface LoginView extends BaseMvpView {
    Context getPersonalContext();

    void launchMain();

    Activity getActivity();
}
