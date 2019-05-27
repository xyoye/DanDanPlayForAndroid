package com.xyoye.dandanplay.mvp.view;

import android.app.Activity;

import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

/**
 * Created by xyoye on 2018/6/29.
 */

public interface PersonalFragmentView extends BaseMvpView {

    void refreshFavorite(AnimeFavoriteBean favoriteBean);

    void refreshHistory(PlayHistoryBean historyBean);

    void refreshUI(AnimeFavoriteBean favoriteBean, PlayHistoryBean historyBean);

    Activity getActivity();
}
