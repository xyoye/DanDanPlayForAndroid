package com.xyoye.dandanplay.ui.animaMod;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimaDetailBean;
import com.xyoye.dandanplay.mvp.impl.AnimaDetailPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimaDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimaDetailView;
import com.xyoye.dandanplay.weight.CornersCenterCrop;
import com.xyoye.dandanplay.weight.ExpandableTextView;
import com.xyoye.dandanplay.weight.ScrollableHelper;
import com.xyoye.dandanplay.weight.ScrollableLayout;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/20.
 */


public class AnimaDetailActivity  extends BaseActivity<AnimaDetailPresenter> implements AnimaDetailView, ScrollableHelper.ScrollableContainer{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.scroll_layout)
    ScrollableLayout scrollableLayout;
    @BindView(R.id.anima_image_iv)
    ImageView animaImageIv;
    @BindView(R.id.anima_title_tv)
    TextView animaTitleTv;
    @BindView(R.id.anima_rating_tv)
    TextView animaTRatingTv;
    @BindView(R.id.anima_onair_tv)
    TextView animaOnairTv;
    @BindView(R.id.anima_airday_tv)
    TextView animaAirdayTv;
    @BindView(R.id.anima_favorited_tv)
    TextView animaFavoritedTv;
    @BindView(R.id.anima_restricted_tv)
    TextView animaRestrictedTv;
    @BindView(R.id.anima_intro_tv)
    ExpandableTextView animaIntroTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<AnimaDetailBean.BangumiBean.EpisodesBean> adapter;

    @Override
    public void initView() {
        setTitle("");
        toolbarTitle.setText("动漫详情");

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected AnimaDetailPresenter initPresenter() {
        return new AnimaDetailPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_anima_detail;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public String getAnimaId() {
        return getIntent().getStringExtra("animaId");
    }

    @Override
    public void showAnimaDetail(AnimaDetailBean bean) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(10));
        Glide.with(this)
                .load(bean.getBangumi().getImageUrl())
                .apply(options)
                .into(animaImageIv);

        animaTitleTv.setText(bean.getBangumi().getAnimeTitle());
        animaTRatingTv.setText("评分："+String.valueOf(bean.getBangumi().getRating()));
        animaOnairTv.setText(bean.getBangumi().isIsOnAir()
                            ? "状态：连载中"
                            : "状态：完结");
        animaAirdayTv.setText(getChineseText(bean.getBangumi().getAirDay()));
        animaFavoritedTv.setText(bean.getBangumi().isIsFavorited()
                                ? "取消关注"
                                : "未关注");
        if (bean.getBangumi().isIsRestricted())
            animaRestrictedTv.setVisibility(View.VISIBLE);
        else
            animaRestrictedTv.setVisibility(View.GONE);

        animaIntroTv.setText("简介："+bean.getBangumi().getSummary());

        //剧集倒序
        List<AnimaDetailBean.BangumiBean.EpisodesBean> episodesList = bean.getBangumi().getEpisodes();
        Collections.reverse(episodesList);
        adapter = new BaseRvAdapter<AnimaDetailBean.BangumiBean.EpisodesBean>(episodesList) {
            @NonNull
            @Override
            public AdapterItem<AnimaDetailBean.BangumiBean.EpisodesBean> onCreateItem(int viewType) {
                return new AnimaEpisodeItem();
            }
        };
        recyclerView.setAdapter(adapter);
        scrollableLayout.getHelper().setCurrentScrollableContainer(this);
    }

    private String getChineseText(int day){
        switch (day){
            case 0:
                return "日期：星期日";
            case 1:
                return "日期：星期一";
            case 2:
                return "日期：星期二";
            case 3:
                return "日期：星期三";
            case 4:
                return "日期：星期四";
            case 5:
                return "日期：星期五";
            case 6:
                return "日期：星期六";
            default:
                return "日期：未知";
        }
    }

    @Override
    public View getScrollableView() {
        return recyclerView;
    }
}
