package com.xyoye.dandanplay.mvp.presenter;

import android.content.Context;

import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/6/29.
 */

public interface PlayFragmentPresenter extends BaseMvpPresenter {

    //刷新视频数据
    void refreshVideo(Context context, boolean reScan);

    //屏蔽目录
    void filterFolder(String folderPath);

    void playLastVideo(Context context, String videoPath);
}
