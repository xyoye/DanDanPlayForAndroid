package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/7/22.
 */

public interface LoginPresenter extends BaseMvpPresenter {
    void login(LoginParam param);
}
