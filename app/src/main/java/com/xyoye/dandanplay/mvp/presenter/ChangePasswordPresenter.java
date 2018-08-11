package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.params.ChangePasswordParam;

/**
 * Created by YE on 2018/8/11.
 */


public interface ChangePasswordPresenter extends BasePresenter{
    void change(ChangePasswordParam param);
}
