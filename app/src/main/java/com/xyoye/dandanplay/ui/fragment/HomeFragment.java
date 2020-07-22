package com.xyoye.dandanplay.ui.fragment;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.mvp.impl.HomeFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.ui.activities.WebViewActivity;
import com.xyoye.dandanplay.ui.activities.anime.AnimeListActivity;
import com.xyoye.dandanplay.ui.activities.anime.AnimeSeasonActivity;
import com.xyoye.dandanplay.ui.activities.anime.SearchActivity;
import com.xyoye.dandanplay.ui.activities.personal.LoginActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.GlideImageLoader;
import com.xyoye.dandanplay.utils.TabEntity;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/6/29.
 */

public class HomeFragment extends BaseMvpFragment<HomeFragmentPresenter> implements HomeFragmentView {
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.tab_layout)
    CommonTabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    AnimeFragmentAdapter fragmentAdapter;
    List<AnimeFragment> fragmentList;

    public static HomeFragment newInstance() {
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
        initTabLayout();
    }

    @Override
    public void initListener() {
        presenter.getHomeFragmentData();
    }

    private void initTabLayout() {
        ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
        mTabEntities.add(new TabEntity("周日", 0, 0));
        mTabEntities.add(new TabEntity("周一", 0, 0));
        mTabEntities.add(new TabEntity("周二", 0, 0));
        mTabEntities.add(new TabEntity("周三", 0, 0));
        mTabEntities.add(new TabEntity("周四", 0, 0));
        mTabEntities.add(new TabEntity("周五", 0, 0));
        mTabEntities.add(new TabEntity("周六", 0, 0));
        tabLayout.setTabData(mTabEntities);
        tabLayout.setCurrentTab(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    @Override
    public void initViewPager(List<BangumiBean> beans) {
        fragmentList = new ArrayList<>();
        for (BangumiBean bean : beans) {
            fragmentList.add(AnimeFragment.newInstance(bean));
        }

        fragmentAdapter = new AnimeFragmentAdapter(getChildFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick({R.id.search_ll, R.id.list_ll, R.id.follow_ll, R.id.history_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.search_ll:
                launchActivity(SearchActivity.class);
                break;
            case R.id.list_ll:
                launchActivity(AnimeSeasonActivity.class);
                break;
            case R.id.follow_ll:
                if (AppConfig.getInstance().isLogin()) {
                    AnimeListActivity.launchAnimeList(getContext(), AnimeListActivity.PERSONAL_FAVORITE);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
            case R.id.history_ll:
                if (AppConfig.getInstance().isLogin()) {
                    AnimeListActivity.launchAnimeList(getContext(), AnimeListActivity.PERSONAL_HISTORY);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
        }
    }

    @Override
    public void setBanners(List<String> images, List<String> titles, List<String> urls) {
        banner.releaseBanner();
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(images);
        banner.setBannerAnimation(Transformer.Default);
        banner.setBannerTitles(titles);
        banner.isAutoPlay(true);
        banner.setDelayTime(5000);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setOnBannerListener(position -> {
            String url = urls.get(position);
            String title = titles.get(position);
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("link", url);
            startActivity(intent);
        });
        banner.start();
    }

    @Override
    public void refreshUI(List<String> images, List<String> titles, List<String> urls, List<BangumiBean> beans) {
        setBanners(images, titles, urls);
        initViewPager(beans);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    private static class AnimeFragmentAdapter extends FragmentPagerAdapter {
        private List<AnimeFragment> list;

        private AnimeFragmentAdapter(FragmentManager supportFragmentManager, List<AnimeFragment> list) {
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
