package com.xyoye.dandanplay.ui.activities.anime;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ConvertUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.AnimeTagBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.impl.AnimeListPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimeListPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeListView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.item.PersonalFavoriteAnimeItem;
import com.xyoye.dandanplay.ui.weight.item.PersonalPlayHistoryItem;
import com.xyoye.dandanplay.ui.weight.item.TagAnimeItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class AnimeListActivity extends BaseMvpActivity<AnimeListPresenter> implements AnimeListView {
    public static final int PERSONAL_FAVORITE = 101;
    public static final int PERSONAL_HISTORY = 102;
    public static final int ANIME_TAG = 103;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    public void initView() {

        recyclerView.addItemDecoration(new ItemDecorationSpaces(ConvertUtils.dp2px(5)));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        int openType = getIntent().getIntExtra("open_type", ANIME_TAG);
        switch (openType){
            case PERSONAL_FAVORITE:
                setTitle("我的关注");
                presenter.getFavorite();
                break;
            case PERSONAL_HISTORY:
                setTitle("播放历史");
                presenter.getPlayHistory();
                break;
            case ANIME_TAG:
                String tagName = getIntent().getStringExtra("tag_name");
                int tagId = getIntent().getIntExtra("tag_id", 0);
                setTitle(tagName);
                presenter.getByTag(tagId);
                break;
        }
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected AnimeListPresenter initPresenter() {
        return new AnimeListPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_anime_list;
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
    public void refreshHistory(PlayHistoryBean playHistoryBean) {
        if (playHistoryBean != null){
            BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean> adapter = new BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean>(playHistoryBean.getPlayHistoryAnimes()) {
                @NonNull
                @Override
                public AdapterItem<PlayHistoryBean.PlayHistoryAnimesBean> onCreateItem(int viewType) {
                    return new PersonalPlayHistoryItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void refreshFavorite(AnimeFavoriteBean animeFavoriteBean) {
        if (animeFavoriteBean != null){
            BaseRvAdapter<AnimeFavoriteBean.FavoritesBean> adapter = new BaseRvAdapter<AnimeFavoriteBean.FavoritesBean>(animeFavoriteBean.getFavorites()) {
                @NonNull
                @Override
                public AdapterItem<AnimeFavoriteBean.FavoritesBean> onCreateItem(int viewType) {
                    return new PersonalFavoriteAnimeItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void refreshTagAnime(AnimeTagBean animeTagBean) {
        if (animeTagBean != null){
            BaseRvAdapter<AnimeTagBean.AnimesBean> adapter = new BaseRvAdapter<AnimeTagBean.AnimesBean>(animeTagBean.getAnimes()) {
                @NonNull
                @Override
                public AdapterItem<AnimeTagBean.AnimesBean> onCreateItem(int viewType) {
                    return new TagAnimeItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }
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

    }

    public static void launchAnimeList(Context context, int openType){
        Intent intent = new Intent(context, AnimeListActivity.class);
        intent.putExtra("open_type", openType);
        context.startActivity(intent);
    }

    public static void launchAnimeListTag(Context context, AnimeDetailBean.BangumiBean.TagsBean tagsBean){
        Intent intent = new Intent(context, AnimeListActivity.class);
        intent.putExtra("tag_id", tagsBean.getId());
        intent.putExtra("tag_name", tagsBean.getName());
        intent.putExtra("open_type", ANIME_TAG);
        context.startActivity(intent);
    }
}
