package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by YE on 2018/7/4 0004.
 */


public interface DanmuNetworkPresenter extends BaseMvpPresenter {
    void matchDanmu(DanmuMatchParam param);

    void searchDanmu(String anime, String episode);
}
