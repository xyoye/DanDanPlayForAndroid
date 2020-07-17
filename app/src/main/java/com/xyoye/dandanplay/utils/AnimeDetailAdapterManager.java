package com.xyoye.dandanplay.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.AnimeDetailEntity;
import com.xyoye.dandanplay.ui.activities.anime.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.activities.anime.SearchActivity;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;

import java.util.Collections;
import java.util.List;

/**
 * Created by xyoye on 2020/7/16.
 *
 * 番剧详情页RecyclerView Adapter
 */

public class AnimeDetailAdapterManager {
    private String animeTitle;
    private String searchWord;

    private boolean isGridEpisode;
    private boolean isAscSort;
    private ItemDecorationSpaces decorationSpaces;
    private AnimeDetailAdapter animeDetailAdapter;

    public AnimeDetailAdapterManager(List<AnimeDetailEntity> data) {
        decorationSpaces = new ItemDecorationSpaces(ConvertUtils.dp2px(4));
        animeDetailAdapter = new AnimeDetailAdapter(data);
    }

    public AnimeDetailAdapter getAnimeDetailAdapter() {
        return animeDetailAdapter;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public class AnimeDetailAdapter extends BaseMultiItemQuickAdapter<AnimeDetailEntity, BaseViewHolder> {

        private AnimeDetailAdapter(List<AnimeDetailEntity> data) {
            super(data);
            addItemType(AnimeDetailEntity.TYPE_EPISODE, R.layout.item_anime_episode_layout);
            addItemType(AnimeDetailEntity.TYPE_RECOMMEND, R.layout.item_anime_recommend_layout);
            addItemType(AnimeDetailEntity.TYPE_MORE, R.layout.item_anime_more_layout);
        }

        @Override
        protected void convert(BaseViewHolder helper, AnimeDetailEntity item) {
            switch (helper.getItemViewType()) {
                //剧集
                case AnimeDetailEntity.TYPE_EPISODE:
                    List<AnimeDetailBean.BangumiBean.EpisodesBean> episodeData = (List<AnimeDetailBean.BangumiBean.EpisodesBean>) item.getObject();
                    AnimeDetailEpisodeAdapter episodeAdapter = new AnimeDetailEpisodeAdapter(R.layout.item_anime_episode, episodeData);
                    RecyclerView episodeRv = helper.getView(R.id.episode_linear_rv);
                    episodeRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                    episodeRv.setAdapter(episodeAdapter);

                    //动态改变rv layout布局
                    ImageView changeLayoutIv = helper.getView(R.id.change_layout_iv);
                    changeLayoutIv.setOnClickListener(v -> {
                        if (isGridEpisode) {
                            episodeRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                            episodeRv.removeItemDecoration(decorationSpaces);
                        } else {
                            episodeRv.setLayoutManager(new GridLayoutManager(mContext, 2));
                            episodeRv.addItemDecoration(decorationSpaces);
                        }
                        isGridEpisode = !isGridEpisode;
                        episodeAdapter.notifyDataSetChanged();

                        //修改图标颜色
                        ColorStateList value = AppCompatResources.getColorStateList(mContext,
                                isGridEpisode ? R.color.immutable_text_theme : R.color.text_gray);
                        ImageViewCompat.setImageTintList(changeLayoutIv, value);
                    });

                    //动态改变rv数据排序
                    ImageView changeSortIv = helper.getView(R.id.change_sort_iv);
                    changeSortIv.setOnClickListener(v -> {
                        Collections.reverse(episodeData);
                        episodeAdapter.notifyDataSetChanged();
                        isAscSort = !isAscSort;

                        //修改图标颜色
                        ColorStateList value = AppCompatResources.getColorStateList(mContext,
                                isAscSort ? R.color.immutable_text_theme : R.color.text_gray);
                        ImageViewCompat.setImageTintList(changeSortIv, value);
                    });
                    break;
                //推荐
                case AnimeDetailEntity.TYPE_RECOMMEND:
                    List<AnimeBean> recommendData = (List<AnimeBean>) item.getObject();
                    AnimeDetailRecommendAdapter recommendAdapter = new AnimeDetailRecommendAdapter(R.layout.item_anime_recommend_v2, recommendData);
                    RecyclerView recommendRv = helper.getView(R.id.recommend_rv);
                    recommendRv.setLayoutManager(new GridLayoutManager(mContext, 2));
                    recommendRv.addItemDecoration(new ItemDecorationSpaces(ConvertUtils.dp2px(4)));
                    recommendRv.setAdapter(recommendAdapter);
                    break;
                //更多
                case AnimeDetailEntity.TYPE_MORE:
                    List<AnimeBean> moreData = (List<AnimeBean>) item.getObject();
                    AnimeDetailMoreAdapter moreAdapter = new AnimeDetailMoreAdapter(R.layout.item_anime_more_v2, moreData);
                    RecyclerView moreRv = helper.getView(R.id.more_rv);
                    moreRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    moreRv.setAdapter(moreAdapter);
                    break;
            }
        }
    }

    private class AnimeDetailEpisodeAdapter extends BaseQuickAdapter<AnimeDetailBean.BangumiBean.EpisodesBean, BaseViewHolder> {

        private AnimeDetailEpisodeAdapter(int layoutResId, @Nullable List<AnimeDetailBean.BangumiBean.EpisodesBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, AnimeDetailBean.BangumiBean.EpisodesBean item) {
            LinearLayout itemLayout = helper.getView(R.id.item_layout);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) itemLayout.getLayoutParams();
            if (isGridEpisode) {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.setMargins(0, 0, 0, 0);
            } else {
                layoutParams.width = ConvertUtils.dp2px(150);
                layoutParams.setMargins(ConvertUtils.dp2px(4), 0, ConvertUtils.dp2px(4), 0);
            }
            itemLayout.setLayoutParams(layoutParams);

            //获取剧集标题及内容
            String info = item.getEpisodeTitle();
            String[] infoArray = info.split("\\s");
            if (infoArray.length > 1) {
                helper.setText(R.id.episode_number, infoArray[0]);
                String title = info.substring(infoArray[0].length() + 1);
                helper.setText(R.id.episode_title, title);
            } else if (infoArray.length == 1) {
                helper.setText(R.id.episode_title, infoArray[0]);
            } else {
                helper.setText(R.id.episode_title, "未知剧集");
            }
            helper.addOnClickListener(R.id.item_layout);

            boolean isLastWatchVisible = isGridEpisode && !TextUtils.isEmpty(item.getLastWatched());
            helper.setText(R.id.last_watch_tv, item.getLastWatched())
                    .setGone(R.id.last_watch_tv, isLastWatchVisible);

            helper.getView(R.id.item_layout).setOnClickListener(v -> {
                String episode = infoArray[0];
                if (episode.startsWith("第") && episode.endsWith("话")) {
                    String temp = episode.substring(1, episode.length() - 1);
                    episode = CommonUtils.isNum(temp) ? temp : episode;
                }
                //进入搜索界面
                Intent intent = new Intent(mContext, SearchActivity.class);
                intent.putExtra("anime_title", animeTitle);
                intent.putExtra("search_word", searchWord + " " + episode);
                intent.putExtra("is_anime", true);
                mContext.startActivity(intent);
            });
        }
    }

    private static class AnimeDetailRecommendAdapter extends BaseQuickAdapter<AnimeBean, BaseViewHolder> {

        private AnimeDetailRecommendAdapter(int layoutResId, @Nullable List<AnimeBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, AnimeBean item) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .transform(new CornersCenterCrop(ConvertUtils.dp2px(5)));

            Glide.with(helper.itemView.getContext())
                    .load(item.getImageUrl())
                    .apply(options)
                    .transition((DrawableTransitionOptions.withCrossFade()))
                    .into((ImageView) helper.getView(R.id.image_iv));

            helper.setText(R.id.title_tv, item.getAnimeTitle())
                    .setText(R.id.type_tv, item.isIsOnAir() ? "连载中" : "已完结")
                    .setText(R.id.rating_tv, item.getRating() + "");

            helper.getView(R.id.item_layout).setOnClickListener(v ->
                    //进入番剧详情界面
                    AnimeDetailActivity.launchAnimeDetail(
                            (Activity) helper.itemView.getContext(),
                            item.getAnimeId() + ""
                    )
            );
        }
    }

    private static class AnimeDetailMoreAdapter extends BaseQuickAdapter<AnimeBean, BaseViewHolder> {

        private AnimeDetailMoreAdapter(int layoutResId, @Nullable List<AnimeBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, AnimeBean item) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .transform(new CornersCenterCrop(ConvertUtils.dp2px(5)));

            Glide.with(helper.itemView.getContext())
                    .load(item.getImageUrl())
                    .apply(options)
                    .transition((DrawableTransitionOptions.withCrossFade()))
                    .into((ImageView) helper.getView(R.id.image_iv));

            helper.setText(R.id.title_tv, item.getAnimeTitle())
                    .setGone(R.id.follow_tv, item.isIsFavorited())
                    .setText(R.id.air_tv, item.isIsOnAir() ? "连载中" : "已完结")
                    .setGone(R.id.type_tv, false)
                    .setGone(R.id.restricted_tv, item.isIsRestricted())
                    .setText(R.id.rating_tv, item.getRating() + "");

            helper.getView(R.id.item_layout).setOnClickListener(v ->
                    //进入番剧详情界面
                    AnimeDetailActivity.launchAnimeDetail(
                            (Activity) helper.itemView.getContext(),
                            item.getAnimeId() + ""
                    )
            );
        }
    }
}
