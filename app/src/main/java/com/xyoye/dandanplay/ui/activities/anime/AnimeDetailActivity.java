package com.xyoye.dandanplay.ui.activities.anime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gyf.immersionbar.ImmersionBar;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.event.SearchMagnetEvent;
import com.xyoye.dandanplay.mvp.impl.AnimeDetailPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimeDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeDetailView;
import com.xyoye.dandanplay.ui.weight.ExpandableTextView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.ScrollableLayout;
import com.xyoye.dandanplay.ui.weight.item.AnimeEpisodeItem;
import com.xyoye.dandanplay.ui.weight.item.AnimeMoreItem;
import com.xyoye.dandanplay.ui.weight.item.AnimeRecommendItem;
import com.xyoye.dandanplay.ui.weight.item.AnimeTagItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/7/20.
 */

public class AnimeDetailActivity extends BaseMvpActivity<AnimeDetailPresenter> implements AnimeDetailView {
    @BindView(R.id.toolbar)
    Toolbar toolBar;
    @BindView(R.id.scroll_layout)
    ScrollableLayout scrollableLayout;
    @BindView(R.id.anime_image_iv)
    ImageView animeImageIv;
    @BindView(R.id.anime_title_tv)
    TextView animeTitleTv;
    @BindView(R.id.anime_status_tv)
    TextView animeStatusTv;
    @BindView(R.id.anime_rate_tv)
    TextView animeRateTv;
    @BindView(R.id.anime_follow_iv)
    ImageView animeFollowIv;
    @BindView(R.id.select_episode_tv)
    TextView selectEpisodeTv;
    @BindView(R.id.anime_intro_tv)
    ExpandableTextView animeIntroTv;
    @BindView(R.id.detail_info_ll)
    LinearLayout detailInfoLL;

    @BindView(R.id.episode_linear_rv)
    RecyclerView episodeLinearRv;
    @BindView(R.id.recommend_rv)
    RecyclerView recommendRv;
    @BindView(R.id.tag_rv)
    RecyclerView tagRv;
    @BindView(R.id.recommend_ll)
    LinearLayout recommendLl;
    @BindView(R.id.more_rv)
    RecyclerView moreRv;
    @BindView(R.id.more_ll)
    LinearLayout moreLl;
    @BindView(R.id.exit_select_iv)
    ImageView exitSelectIv;
    @BindView(R.id.episode_grid_rv)
    RecyclerView episodeGridRv;
    @BindView(R.id.episode_ll)
    LinearLayout normalEpisodeLL;
    @BindView(R.id.recommend_all_ll)
    LinearLayout recommendAllLL;
    @BindView(R.id.select_episode_ll)
    LinearLayout selectEpisodeLl;

    private AnimeDetailBean animeDetailBean;
    private boolean isFavorite = false;
    private int toolbarHeight;
    private String animeId = "";

    private BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean> episodeLinearAdapter;
    private BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean> episodeGridAdapter;
    private BaseRvAdapter<AnimeBean> recommendAdapter;
    private BaseRvAdapter<AnimeBean> moreAdapter;
    private BaseRvAdapter<AnimeDetailBean.BangumiBean.TagsBean> tagAdapter;

    private List<AnimeDetailBean.BangumiBean.EpisodesBean> episodeLinearList;
    private List<AnimeDetailBean.BangumiBean.EpisodesBean> episodeGridList;
    private List<AnimeBean> recommendList;
    private List<AnimeBean> moreList;
    private List<AnimeDetailBean.BangumiBean.TagsBean> tagList;

    @Override
    protected void setStatusBar() {
        ImmersionBar.with(this)
                .transparentBar()
                .fitsSystemWindows(false)
                .init();
    }

    @Override
    public void initView() {
        int statusBarHeight = ConvertUtils.dp2px(20);
        toolBar.setPadding(0, statusBarHeight, 0, 0);
        ViewGroup.LayoutParams toolbarParams = toolBar.getLayoutParams();
        toolbarParams.height += statusBarHeight;
        toolbarHeight = toolbarParams.height;

        scrollableLayout.addHeadView(toolBar);
        toolBar.setBackgroundColor(CommonUtils.getResColor(0, R.color.theme_color));
        toolBar.setTitleTextColor(CommonUtils.getResColor(0, R.color.immutable_text_white));

        episodeLinearList = new ArrayList<>();
        episodeGridList = new ArrayList<>();
        recommendList = new ArrayList<>();
        moreList = new ArrayList<>();
        tagList = new ArrayList<>();

        episodeLinearAdapter = new BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean>(episodeLinearList) {
            @NonNull
            @Override
            public AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> onCreateItem(int viewType) {
                return new AnimeEpisodeItem(false);
            }
        };

        episodeGridAdapter = new BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean>(episodeGridList) {
            @NonNull
            @Override
            public AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> onCreateItem(int viewType) {
                return new AnimeEpisodeItem(true);
            }
        };

        recommendAdapter = new BaseRvAdapter<AnimeBean>(recommendList) {
            @NonNull
            @Override
            public AdapterItem<AnimeBean> onCreateItem(int viewType) {
                return new AnimeRecommendItem();
            }
        };

        moreAdapter = new BaseRvAdapter<AnimeBean>(moreList) {
            @NonNull
            @Override
            public AdapterItem<AnimeBean> onCreateItem(int viewType) {
                return new AnimeMoreItem();
            }
        };

        tagAdapter = new BaseRvAdapter<AnimeDetailBean.BangumiBean.TagsBean>(tagList) {
            @NonNull
            @Override
            public AdapterItem<AnimeDetailBean.BangumiBean.TagsBean> onCreateItem(int viewType) {
                return new AnimeTagItem();
            }
        };

        episodeLinearRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        episodeLinearRv.setNestedScrollingEnabled(false);
        episodeLinearRv.addItemDecoration(new ItemDecorationSpaces(10));
        episodeLinearRv.setAdapter(episodeLinearAdapter);

        episodeGridRv.setLayoutManager(new GridLayoutManager(this, 2));
        episodeGridRv.addItemDecoration(new ItemDecorationSpaces(0, 10, 10, 10, 2));
        episodeGridRv.setAdapter(episodeGridAdapter);

        recommendRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendRv.setNestedScrollingEnabled(false);
        recommendRv.addItemDecoration(new ItemDecorationSpaces(10));
        recommendRv.setAdapter(recommendAdapter);

        moreRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        moreRv.setNestedScrollingEnabled(false);
        moreRv.setAdapter(moreAdapter);

        tagRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tagRv.setAdapter(tagAdapter);

        animeId = getIntent().getStringExtra("anime_id");
        presenter.getAnimeDetail(animeId);

        scrollableLayout.setHeadCount(2);
    }

    @Override
    public void initListener() {
        scrollableLayout.getHelper().setCurrentScrollableContainer(() -> {
            if (scrollableLayout.getHeadCount() == 2) {
                return moreRv;
            } else {
                return episodeGridRv;
            }
        });

        scrollableLayout.setOnScrollListener((currentY, maxY) -> {
            //从详情信息头部计算位移
            int scrollY = currentY - animeImageIv.getTop();
            //最大的计算范围为详情底部
            int maxScrollY = animeImageIv.getBottom() - toolbarHeight - animeImageIv.getTop();

            //未到达详情头部一律透明
            if (scrollY < 0) {
                toolBar.setBackgroundColor(CommonUtils.getResColor(0, R.color.theme_color));
                toolBar.setTitleTextColor(CommonUtils.getResColor(0, R.color.immutable_text_white));
            }
            //大于详情底部一律不透明
            else if (scrollY > maxScrollY) {
                toolBar.setBackgroundColor(CommonUtils.getResColor(R.color.theme_color));
                toolBar.setTitleTextColor(CommonUtils.getResColor(R.color.immutable_text_white));
            }
            //按位移量计算计算透明度
            else {
                int alpha = (scrollY * 255) / maxScrollY;
                toolBar.setBackgroundColor(CommonUtils.getResColor(alpha, R.color.theme_color));
                toolBar.setTitleTextColor(CommonUtils.getResColor(alpha, R.color.immutable_text_white));
            }
        });
    }

    @NonNull
    @Override
    protected AnimeDetailPresenter initPresenter() {
        return new AnimeDetailPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_anime_detail;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void showAnimeDetail(AnimeDetailBean bean) {
        animeDetailBean = bean;
        //封面
        Glide.with(this)
                .load(bean.getBangumi().getImageUrl())
                .transition((DrawableTransitionOptions.withCrossFade()))
                .into(animeImageIv);

        this.setTitle(bean.getBangumi().getAnimeTitle());

        //标题
        animeTitleTv.setText(bean.getBangumi().getAnimeTitle());

        //标签
        tagList.addAll(bean.getBangumi().getTags());
        tagAdapter.notifyDataSetChanged();

        //更新状态
        animeStatusTv.setText(getAnimeStatus(bean.getBangumi()));

        //评分
        double rating = bean.getBangumi().getRating();
        String rateText = rating <= 0
                ? "0"
                : new DecimalFormat("0.0").format(rating);
        animeRateTv.setText(rateText);

        //关注状态
        if (bean.getBangumi().isIsFavorited()) {
            animeFollowIv.setImageResource(R.mipmap.ic_follow_yes);
            isFavorite = true;
        } else {
            animeFollowIv.setImageResource(R.mipmap.ic_follow_no);
            isFavorite = false;
        }

        //介绍
        String summary = StringUtils.isEmpty(bean.getBangumi().getSummary()) ? "无" : bean.getBangumi().getSummary();
        animeIntroTv.post(() ->
                //OnCreate时view可能没有绘制完成，getMeasuredWidth为0
                animeIntroTv.setText("简介：" + summary, detailInfoLL.getMeasuredWidth()));

        //剧集
        episodeGridList.addAll(bean.getBangumi().getEpisodes());
        episodeLinearList.addAll(bean.getBangumi().getEpisodes());
        Collections.reverse(episodeGridList);
        episodeGridAdapter.notifyDataSetChanged();
        episodeLinearAdapter.notifyDataSetChanged();
        //相关推荐
        if (bean.getBangumi().getRelateds() != null && bean.getBangumi().getRelateds().size() > 0) {
            recommendLl.setVisibility(View.VISIBLE);
            recommendList.addAll(bean.getBangumi().getRelateds());
            recommendAdapter.notifyDataSetChanged();
        }
        //更多推荐
        if (bean.getBangumi().getSimilars() != null && bean.getBangumi().getSimilars().size() > 0) {
            moreLl.setVisibility(View.VISIBLE);
            moreList.addAll(bean.getBangumi().getSimilars());
            moreAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void afterFollow(boolean isFollow) {
        if (isFollow) {
            isFavorite = true;
            animeFollowIv.setImageResource(R.mipmap.ic_follow_yes);
            ToastUtils.showShort("关注成功");
        } else {
            isFavorite = false;
            animeFollowIv.setImageResource(R.mipmap.ic_follow_no);
            ToastUtils.showShort("取消关注成功");
        }
    }

    private String getAnimeStatus(AnimeDetailBean.BangumiBean bangumiBean) {
        if (!bangumiBean.isIsOnAir())
            return "已完结";
        String onAirDay;
        switch (bangumiBean.getAirDay()) {
            case 0:
                onAirDay = "每周日更新";
                break;
            case 1:
                onAirDay = "每周一更新";
                break;
            case 2:
                onAirDay = "每周二更新";
                break;
            case 3:
                onAirDay = "每周三更新";
                break;
            case 4:
                onAirDay = "每周四更新";
                break;
            case 5:
                onAirDay = "每周五更新";
                break;
            case 6:
                onAirDay = "每周六更新";
                break;
            default:
                onAirDay = "更新日期未知";
                break;
        }
        return "连载中 · " + onAirDay;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchMagnetEvent event) {
        String episode = event.getEpisodeName();
        if (episode.startsWith("第") && episode.endsWith("话")) {
            String temp = episode.substring(1, episode.length() - 1);
            episode = CommonUtils.isNum(temp) ? temp : episode;
        }
        Intent intent = new Intent(AnimeDetailActivity.this, SearchActivity.class);
        intent.putExtra("anime_title", animeDetailBean.getBangumi().getAnimeTitle());
        intent.putExtra("search_word", animeDetailBean.getBangumi().getSearchKeyword() + " " + episode);
        intent.putExtra("is_anime", true);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void showLoading() {
        //showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        //dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @OnClick({R.id.anime_image_iv, R.id.select_episode_tv, R.id.exit_select_iv, R.id.anime_follow_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.anime_image_iv:
                if (animeDetailBean != null) {
                    Intent intent = new Intent(AnimeDetailActivity.this, ImagePreviewActivity.class);
                    intent.putExtra("image_url", animeDetailBean.getBangumi().getImageUrl());
                    startActivity(intent);
                }
                break;
            case R.id.select_episode_tv:
                scrollableLayout.setHeadCount(1);
                selectEpisodeLl.setVisibility(View.VISIBLE);
                normalEpisodeLL.setVisibility(View.GONE);
                recommendAllLL.setVisibility(View.GONE);
                break;
            case R.id.exit_select_iv:
                scrollableLayout.setHeadCount(2);
                selectEpisodeLl.setVisibility(View.GONE);
                recommendAllLL.setVisibility(View.VISIBLE);
                normalEpisodeLL.setVisibility(View.VISIBLE);
                break;
            case R.id.anime_follow_iv:
                if (AppConfig.getInstance().isLogin()) {
                    if (isFavorite) {
                        presenter.followCancel(animeId);
                    } else {
                        presenter.followConfirm(animeId);
                    }
                } else {
                    ToastUtils.showShort(R.string.anime_detail_not_login_hint);
                }
                break;
        }
    }

    public static void launchAnimeDetail(Activity activity, String animeId) {
        Intent intent = new Intent(activity, AnimeDetailActivity.class);
        intent.putExtra("anime_id", animeId);
        activity.startActivity(intent);
    }
}
