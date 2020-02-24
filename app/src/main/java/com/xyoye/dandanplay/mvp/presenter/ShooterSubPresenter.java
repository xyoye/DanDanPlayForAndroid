package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface ShooterSubPresenter extends BaseMvpPresenter {
    void updateQuota();

    void searchSubtitle(String text, int page);

    void querySubtitleDetail(int id);

    void downloadSubtitleFile(String fileName, String downloadLink);
}
