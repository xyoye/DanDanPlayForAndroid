package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/5/14.
 */

public interface ScanManagerPresenter extends BaseMvpPresenter {
    void queryFormSystem(VideoBean videoBean, String path);

    boolean saveNewVideo(VideoBean videoBean);

    void listFolder(String path);
}
