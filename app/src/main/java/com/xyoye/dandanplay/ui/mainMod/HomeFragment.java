package com.xyoye.dandanplay.ui.mainMod;

import android.support.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.HomeFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.utils.GlideImageLoader;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class HomeFragment extends BaseFragment<HomeFragmentPresenter> implements HomeFragmentView{
    @BindView(R.id.banner)
    Banner banner;

    public static HomeFragment newInstance(){
        return new HomeFragment();
    }

    @NonNull
    @Override
    protected HomeFragmentPresenter initPresenter() {
        return new HomeFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void setBanners(List<String> images, List<String> titles, List<String> urls) {
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(images);
        banner.setBannerAnimation(Transformer.DepthPage);
        banner.setBannerTitles(titles);
        banner.isAutoPlay(true);
        banner.setDelayTime(3000);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setOnBannerListener(position -> {
            String url = urls.get(position);
            ToastUtils.showShort(url);
        });
        banner.start();
    }
}
