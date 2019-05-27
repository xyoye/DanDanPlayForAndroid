package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.params.ChangePasswordParam;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/8/11.
 */

public interface ChangePasswordPresenter extends BaseMvpPresenter {
    void change(ChangePasswordParam param);
}
