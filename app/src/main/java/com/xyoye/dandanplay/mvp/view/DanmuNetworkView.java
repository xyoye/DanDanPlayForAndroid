package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2018/7/4 0004.
 */


public interface DanmuNetworkView extends BaseMvpView , LoadDataView{

    void refreshAdapter(List<DanmuMatchBean.MatchesBean> matches);
}
