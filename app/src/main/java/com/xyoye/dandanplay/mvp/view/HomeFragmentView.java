package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.AnimeBeans;

import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface HomeFragmentView extends BaseMvpView{
    void setBanners(List<String> images, List<String> titles, List<String> urls);

    void initViewPager(List<AnimeBeans> beans);

    void refreshUI(List<String> images, List<String> titles, List<String> urls, List<AnimeBeans> beans);
}
