package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;

/**
 * Created by YE on 2018/6/30 0030.
 */


public interface FolderPresenter extends BasePresenter {
    void getVideoList(String folderPath);

    void updateDanmu(String danmuPath, int episodeId, String[] whereArgs);

    void updateCurrent(SaveCurrentEvent event);

    void deleteFile(String filePath);

    void getDanmu(String videoPath);

    void observeService(VideoBean videoBean);
}
