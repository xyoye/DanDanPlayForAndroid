package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyy on 2019/1/9.
 */

public interface AnimaSeasonView extends BaseMvpView, LoadDataView{

    void refreshAnimas(List<AnimeBeans.BangumiListBean> animas);
}
