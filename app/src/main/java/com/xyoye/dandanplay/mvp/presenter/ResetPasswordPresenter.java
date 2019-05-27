package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/8/11.
 */

public interface ResetPasswordPresenter extends BaseMvpPresenter {
    void reset(ResetPasswordParam param);
}
