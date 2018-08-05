package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.params.RegisterParam;

/**
 * Created by YE on 2018/8/5.
 */


public interface RegisterPresenter extends BasePresenter {
    void register(RegisterParam param);
}
