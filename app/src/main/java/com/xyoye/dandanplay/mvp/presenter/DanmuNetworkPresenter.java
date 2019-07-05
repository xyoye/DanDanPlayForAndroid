package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/7/4 0004.
 */

public interface DanmuNetworkPresenter extends BaseMvpPresenter {
    void matchDanmu(String videoPath);

    void searchDanmu(String anime, String episode);
}
