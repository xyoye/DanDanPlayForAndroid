package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by YE on 2018/8/5.
 */


public interface RegisterPresenter extends BaseMvpPresenter {
    void register(RegisterParam param);
}
