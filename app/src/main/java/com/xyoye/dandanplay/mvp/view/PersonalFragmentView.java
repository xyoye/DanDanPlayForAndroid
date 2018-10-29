package com.xyoye.dandanplay.mvp.view;

import android.app.Activity;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PersonalFragmentView extends BaseMvpView {

    void refreshFavorite(AnimeFavoriteBean favoriteBean);

    void refreshHistory(PlayHistoryBean historyBean);

    void refreshUI(AnimeFavoriteBean favoriteBean, PlayHistoryBean historyBean);

    Activity getActivity();
}
