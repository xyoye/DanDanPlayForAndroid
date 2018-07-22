package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.params.LoginParam;

/**
 * Created by YE on 2018/7/22.
 */


public interface LoginPresenter extends BasePresenter {
    void login(LoginParam param);
}
