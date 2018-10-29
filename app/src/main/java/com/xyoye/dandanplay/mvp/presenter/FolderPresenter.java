package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;

/**
 * Created by YE on 2018/6/30 0030.
 */


public interface FolderPresenter extends BasePresenter {
    void refreshVideos();

    void updateDanmu(String danmuPath, int episodeId, String[] whereArgs);

    void updateCurrent(SaveCurrentEvent event);
}
