package com.xyoye.dandanplay.ui.activities.anime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gyf.immersionbar.ImmersionBar;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.AnimeDetailEntity;
import com.xyoye.dandanplay.mvp.impl.AnimeDetailPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimeDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeDetailView;
import com.xyoye.dandanplay.ui.weight.ExpandableTextView;
import com.xyoye.dandanplay.ui.weight.item.AnimeTagItem;
import com.xyoye.dandanplay.utils.AnimeDetailAdapterManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.view.WindowUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/7/20.
 */

public class AnimeDetailActivity extends BaseMvpActivity<AnimeDetailPresenter> implements AnimeDetailView, WindowUtils.InsetsListener {
    @BindView(R.id.toolbar)
    Toolbar toolBar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
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
    @BindView(R.id.anime_intro_tv)
    ExpandableTextView animeIntroTv;
    @BindView(R.id.detail_info_ll)
    LinearLayout detailInfoLL;

    @BindView(R.id.tag_rv)
    RecyclerView tagRv;
    @BindView(R.id.anime_detail_rv)
    RecyclerView animeDetailRv;

    private AnimeDetailBean animeDetailBean;
    private boolean isFavorite = false;
    private String animeId = "";

    private BaseRvAdapter<AnimeDetailBean.BangumiBean.TagsBean> tagAdapter;
    private List<AnimeDetailBean.BangumiBean.TagsBean> tagList;
    private AnimeDetailAdapterManager adapterManager;
    private AnimeDetailAdapterManager.AnimeDetailAdapter animeDetailAdapter;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_anime_detail;
    }

    @NonNull
    @Override
    protected AnimeDetailPresenter initPresenter() {
        return new AnimeDetailPresenterImpl(this, this);
    }

    @Override
    protected void setStatusBar() {
        ImmersionBar.with(this)
                .transparentBar()
                .fitsSystemWindows(false)
                .init();
    }

    @Override
    public void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowUtils.doOnApplyWindowInsets(appBarLayout, this);
            WindowUtils.requestApplyInsetsWhenAttached(appBarLayout);
        } else {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            toolBar.setPadding(0, statusBarHeight, 0, 0);
            ViewGroup.LayoutParams toolbarParams = toolBar.getLayoutParams();
            toolbarParams.height += statusBarHeight;
            toolBar.setLayoutParams(toolbarParams);
        }

        toolBar.setBackgroundColor(CommonUtils.getResColor(0, R.color.theme_color));
        toolBar.setTitleTextColor(CommonUtils.getResColor(0, R.color.immutable_text_white));
        collapsingToolbarLayout.setCollapsedTitleTextColor(CommonUtils.getResColor(R.color.immutable_text_white));

        tagList = new ArrayList<>();
        tagAdapter = new BaseRvAdapter<AnimeDetailBean.BangumiBean.TagsBean>(tagList) {
            @NonNull
            @Override
            public AdapterItem<AnimeDetailBean.BangumiBean.TagsBean> onCreateItem(int viewType) {
                return new AnimeTagItem();
            }
        };
        tagRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tagRv.setAdapter(tagAdapter);

        adapterManager = new AnimeDetailAdapterManager(new ArrayList<>());
        animeDetailAdapter = adapterManager.getAnimeDetailAdapter();
        animeDetailRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        animeDetailRv.setAdapter(animeDetailAdapter);

        animeId = getIntent().getStringExtra("anime_id");
        presenter.getAnimeDetail(animeId);
    }

    @Override
    public void initListener() {
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) ->
                collapsingToolbarLayout.setTitleEnabled(
                        Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()
                )
        );
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

        String animeTitle = bean.getBangumi().getAnimeTitle();
        collapsingToolbarLayout.setTitle(animeTitle);

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

        List<AnimeDetailEntity> entityList = new ArrayList<>();
        //剧集
        entityList.add(new AnimeDetailEntity(AnimeDetailEntity.TYPE_EPISODE, bean.getBangumi().getEpisodes()));
        //相关推荐
        if (bean.getBangumi().getRelateds() != null && bean.getBangumi().getRelateds().size() > 0) {
            entityList.add(new AnimeDetailEntity(AnimeDetailEntity.TYPE_RECOMMEND, bean.getBangumi().getRelateds()));
        }
        //更多推荐
        if (bean.getBangumi().getSimilars() != null && bean.getBangumi().getSimilars().size() > 0) {
            entityList.add(new AnimeDetailEntity(AnimeDetailEntity.TYPE_MORE, bean.getBangumi().getSimilars()));
        }

        adapterManager.setAnimeTitle(bean.getBangumi().getAnimeTitle());
        adapterManager.setSearchWord(bean.getBangumi().getSearchKeyword());
        animeDetailAdapter.replaceData(entityList);
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

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @OnClick({R.id.anime_image_iv, R.id.anime_follow_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.anime_image_iv:
                if (animeDetailBean != null) {
                    Intent intent = new Intent(AnimeDetailActivity.this, ImagePreviewActivity.class);
                    intent.putExtra("image_url", animeDetailBean.getBangumi().getImageUrl());
                    startActivity(intent);
                }
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

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View view, WindowUtils.Padding padding, WindowUtils.Padding margin, WindowInsetsCompat insets) {
        int statusBarHeight = padding.getTop() + insets.getSystemWindowInsetTop();

        ViewGroup.LayoutParams toolbarParams = toolBar.getLayoutParams();
        toolBar.setPadding(toolBar.getPaddingLeft(), statusBarHeight, toolBar.getPaddingRight(), toolBar.getPaddingBottom());
        toolbarParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        toolBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        toolbarParams.height = toolBar.getMeasuredHeight();
        return insets;
    }


    public static void launchAnimeDetail(Activity activity, String animeId) {
        Intent intent = new Intent(activity, AnimeDetailActivity.class);
        intent.putExtra("anime_id", animeId);
        activity.startActivity(intent);
    }
}
