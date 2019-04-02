package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by YE on 2018/6/30 0030.
 */


public interface FolderView extends BaseMvpView, LoadDataView {

    void refreshAdapter(List<VideoBean> beans);

    void downloadDanmu(DanmuMatchBean.MatchesBean matchesBean);

    void noMatchDanmu(String videoPath);

    void openIntentVideo(VideoBean videoBean);
}
