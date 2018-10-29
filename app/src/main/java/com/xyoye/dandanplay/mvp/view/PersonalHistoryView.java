package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.core.interf.view.LoadDataView;
import com.xyoye.dandanplay.bean.PlayHistoryBean;

/**
 * Created by YE on 2018/7/24.
 */


public interface PersonalHistoryView extends BaseMvpView, LoadDataView {
    void refreshHistory(PlayHistoryBean playHistoryBean);
}
