package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.AnimaBeans;

import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface HomeFragmentView extends BaseMvpView{
    void setBanners(List<String> images, List<String> titles, List<String> urls);

    void initViewPager(List<AnimaBeans> beans, List<String> dateList);
}
