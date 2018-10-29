package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;

/**
 * Created by YE on 2018/7/4 0004.
 */


public interface DanmuNetworkPresenter extends BasePresenter {
    void matchDanmu(DanmuMatchParam param);

    void searchDanmu(String anime, String episode);
}
