package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by xyoye on 2018/7/24.
 */

public interface PersonalHistoryView extends BaseMvpView, LoadDataView {
    void refreshHistory(PlayHistoryBean playHistoryBean);
}
