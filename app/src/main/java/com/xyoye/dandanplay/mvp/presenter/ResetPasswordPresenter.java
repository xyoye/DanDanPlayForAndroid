package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;

/**
 * Created by YE on 2018/8/11.
 */


public interface ResetPasswordPresenter extends BasePresenter {
    void reset(ResetPasswordParam param);
}
