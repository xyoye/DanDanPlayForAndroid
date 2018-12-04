package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by YE on 2018/10/13.
 */


public interface SearchMagnetPresenter extends BaseMvpPresenter {

    void searchMagnet(String anime, int typeId, int subGroundId);

    void downloadTorrent(String animeTitle, String magnet);

    List<AnimeTypeBean.TypesBean> getTypeList();

    List<SubGroupBean.SubgroupsBean> getSubGroupList();
}
