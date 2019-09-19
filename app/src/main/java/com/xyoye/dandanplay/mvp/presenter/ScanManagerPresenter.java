package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyoye on 2019/5/14.
 */

public interface ScanManagerPresenter extends BaseMvpPresenter {

    void saveNewVideo(List<String> videoPath);

    void listFolder(String path);
}
