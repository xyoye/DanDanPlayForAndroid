package com.xyoye.dandanplay.ui.mainMod;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.rx.LifefulRunnable;
import com.xyoye.core.utils.PixelUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimaBeans;
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
import com.xyoye.dandanplay.weight.DiyTablayout.title.ClipPagerTitleView;
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
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.magic_indicator)
    MagicIndicator magicIndicator;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    AnimaFragmentAdapter fragmentAdapter;
    List<AnimaFragment> fragmentList;

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
            String title = titles.get(position);
            Intent intent = new Intent(getContext(), WebviewActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("link", url);
            startActivity(intent);
        });
        banner.start();
    }

    @Override
    public void initViewPager(List<AnimaBeans> beans, List<String> dateLis) {
        fragmentList = new ArrayList<>();
        for (AnimaBeans bean : beans) {
            fragmentList.add(AnimaFragment.newInstance(bean));
        }
        fragmentAdapter = new AnimaFragmentAdapter(getFragmentManager(), fragmentList);

        CommonNavigator commonNavigator = new CommonNavigator(getBaseActivity());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return dateLis.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                ClipPagerTitleView clipPagerTitleView = new ClipPagerTitleView(context);
                clipPagerTitleView.setText(dateLis.get(index));
                clipPagerTitleView.setPadding(PixelUtil.dip2px(context, 15), 0, PixelUtil.dip2px(context, 15), 0);
                clipPagerTitleView.setTextColor(Color.parseColor("#222222"));
                clipPagerTitleView.setClipColor(Color.WHITE);
                clipPagerTitleView.setOnClickListener(v -> viewPager.setCurrentItem(index));
                return clipPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                float navigatorHeight = context.getResources().getDimension(R.dimen.common_navigator_height);
                float borderWidth = PixelUtil.dip2px(context, 8);
                float lineHeight = navigatorHeight - 2 * borderWidth;
                indicator.setLineHeight(lineHeight);
                indicator.setRoundRadius(lineHeight / 2);
                indicator.setYOffset(borderWidth);
                indicator.setColors(Color.parseColor("#FF2C6B"));
                return indicator;
            }
        });

        bindViewPager(magicIndicator, viewPager);
        //viewPager.setOffscreenPageLimit(2);

        getBaseActivity().runOnUiThread(new LifefulRunnable(() -> {
            viewPager.setAdapter(fragmentAdapter);
            magicIndicator.setNavigator(commonNavigator);
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

    class AnimaFragmentAdapter extends FragmentPagerAdapter {
        private List<AnimaFragment> list;

        public AnimaFragmentAdapter(FragmentManager supportFragmentManager, List<AnimaFragment> list) {
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
