package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface ShooterSubView extends BaseMvpView, LoadDataView {
    void updateQuota(int quota);

    void updateSubtitleList(List<ShooterSubtitleBean.SubBean.SubsBean> subtitleList, boolean enableLoadMore);

    void updateSubtitleFailed();

    void showSubtitleDetailDialog(ShooterSubDetailBean.SubBean.SubsBean detailBean);

    void subtitleDownloadSuccess();
}
