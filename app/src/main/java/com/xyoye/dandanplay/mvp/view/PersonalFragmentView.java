package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PersonalFragmentView extends BaseMvpView {

    void changeView();

    void refreshFavorite(AnimeFavoriteBean favoriteBean);

    void refreshHistory(PlayHistoryBean historyBean);
}
