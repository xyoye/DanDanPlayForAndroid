package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;
import com.xyoye.player.commom.bean.SubtitleBean;

import java.util.List;

/**
 * Created by xyoye on 2018/7/4 0004.
 */


public interface BindZimuView extends BaseMvpView, LoadDataView {

    void refreshZimuAdapter(List<SubtitleBean> matches);

    void updateSubtitleList(List<ShooterSubtitleBean.SubBean.SubsBean> subtitleList, boolean enableLoadMore);

    void updateSubtitleFailed();

    void showSubtitleDetailDialog(ShooterSubDetailBean.SubBean.SubsBean detailBean);

    void subtitleDownloadSuccess(String resultFilePath, boolean unzip);
}
