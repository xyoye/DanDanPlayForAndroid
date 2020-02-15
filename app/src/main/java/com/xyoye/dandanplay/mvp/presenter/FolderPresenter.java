package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/6/30.
 */


public interface FolderPresenter extends BaseMvpPresenter {
    void getVideoList(String folderPath);

    void updateDanmu(String danmuPath, int episodeId, String[] whereArgs);

    void updateZimu(String zimuPath, String[] whereArgs);

    void getDanmu(String videoPath);
}
