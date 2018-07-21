package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.AnimaDetailBean;

/**
 * Created by YE on 2018/7/20.
 */


public interface AnimaDetailView extends BaseMvpView {

    String getAnimaId();

    void showAnimaDetail(AnimaDetailBean bean);
}
