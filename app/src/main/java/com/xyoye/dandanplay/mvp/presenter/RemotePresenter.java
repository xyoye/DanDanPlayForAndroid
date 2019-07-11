package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/7/11.
 */

public interface RemotePresenter extends BaseMvpPresenter {
    void getVideoList(String ip, int port, String authorization);

    void bindRemoteDanmu(String hash, String danmuName);
}
