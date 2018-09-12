package com.xyoye.dandanplay.ui.homeMod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.rx.LifefulRunnable;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.mvp.impl.HomeFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.ui.webMod.WebviewActivity;
import com.xyoye.dandanplay.utils.GlideImageLoader;
import com.xyoye.dandanplay.weight.DiyTablayout.CommonNavigator.CommonNavigator;
import com.xyoye.dandanplay.weight.DiyTablayout.LinePagerIndicator;
import com.xyoye.dandanplay.weight.DiyTablayout.MagicIndicator;
import com.xyoye.dandanplay.weight.DiyTablayout.abs.CommonNavigatorAdapter;
import com.xyoye.dandanplay.weight.DiyTablayout.abs.IPagerIndicator;
import com.xyoye.dandanplay.weight.DiyTablayout.abs.IPagerTitleView;
import com.xyoye.dandanplay.weight.DiyTablayout.title.ColorTransitionPagerTitleView;
import com.xyoye.dandanplay.weight.DiyTablayout.title.SimplePagerTitleView;
import com.xyoye.dandanplay.weight.ScrollableLayout;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class HomeFragment extends BaseFragment<HomeFragmentPresenter> implements HomeFragmentView{
    @BindView(R.id.scroll_layout)
    ScrollableLayout scrollableLayout;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.magic_indicator)
    MagicIndicator magicIndicator;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    AnimaFragmentAdapter fragmentAdapter;
    List<AnimeFragment> fragmentList;

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
        banner.setDelayTime(5000);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setOnBannerListener(position -> {
            String url = urls.get(position);
            String title = titles.get(position);
            Intent intent = new Intent(getContext(), WebviewActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("link", url);
            startActivity(intent);
        });
        banner.start();
    }

    @Override
    public void initIndicator(List<String> dateList) {
        CommonNavigator commonNavigator = new CommonNavigator(getBaseActivity());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return dateList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setText(dateList.get(index));
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(context, R.color.text_black));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(context, R.color.theme_color));
                simplePagerTitleView.setOnClickListener(v -> viewPager.setCurrentItem(index));
                return simplePagerTitleView;
            }

            @SuppressLint("ResourceType")
            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setColors(ContextCompat.getColor(context, R.color.theme_color));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
    }

    @Override
    public void initViewPager(List<AnimeBeans> beans) {
        fragmentList = new ArrayList<>();
        for (AnimeBeans bean : beans) {
            fragmentList.add(AnimeFragment.newInstance(bean));
        }

        bindViewPager(magicIndicator, viewPager);

        getBaseActivity().runOnUiThread(new LifefulRunnable(() -> {

            fragmentAdapter = new AnimaFragmentAdapter(getChildFragmentManager(), fragmentList);

            if (fragmentList.size() > 0) {
                scrollableLayout.getHelper().setCurrentScrollableContainer(fragmentList.get(0));
            }
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    scrollableLayout.getHelper().setCurrentScrollableContainer(fragmentList.get(position));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            viewPager.setAdapter(fragmentAdapter);

            viewPager.setOffscreenPageLimit(2);
        }, this));
    }

    public static void bindViewPager(final MagicIndicator magicIndicator, ViewPager viewPager) {
        if (viewPager == null) return;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

    private class AnimaFragmentAdapter extends FragmentPagerAdapter {
        private List<AnimeFragment> list;

        private AnimaFragmentAdapter(FragmentManager supportFragmentManager, List<AnimeFragment> list) {
            super(supportFragmentManager);

            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
