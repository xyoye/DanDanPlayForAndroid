package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.core.interf.view.LoadDataView;
import com.xyoye.dandanplay.bean.DanmuMatchBean;

import java.util.List;

/**
 * Created by YE on 2018/7/4 0004.
 */


public interface DanmuNetworkView extends BaseMvpView , LoadDataView{
    String getVideoPath();

    boolean isLan();

    void refreshAdapter(List<DanmuMatchBean.MatchesBean> matches);
}
