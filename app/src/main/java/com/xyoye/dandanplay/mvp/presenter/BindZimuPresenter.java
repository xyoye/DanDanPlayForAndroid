package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/7/4 0004.
 */

public interface BindZimuPresenter extends BaseMvpPresenter {

    void matchZimu(String videoPath);

    void searchZimu(String videoName, int page);

    void queryZimuDetail(int subtitleId);

    void downloadSubtitleFile(String subtitleName, String downloadLink);

    void downloadSubtitleFile(String fileName, String downloadLink, boolean unzip);
}
