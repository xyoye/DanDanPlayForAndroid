package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;

import java.util.List;

/**
 * Created by YE on 2018/10/13.
 */


public interface SearchMagnetPresenter extends BasePresenter{

    void searchMagnet(String anime, int typeId, int subGroundId);

    void downloadTorrent(String animeTitle, String magnet);

    List<AnimeTypeBean.TypesBean> getTypeList();

    List<SubGroupBean.SubgroupsBean> getSubGroupList();
}
