package com.xyoye.dandanplay.ui.weight.item;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.ui.activities.anime.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class PersonalFavoriteAnimeItem implements AdapterItem<AnimeFavoriteBean.FavoritesBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.anime_title)
    TextView animeTitle;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(AnimeFavoriteBean.FavoritesBean model, int position) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(3));

        Glide.with(imageView.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .transition((DrawableTransitionOptions.withCrossFade()))
                .into(imageView);

        animeTitle.setText(model.getAnimeTitle());

        mView.setOnClickListener(v ->
                AnimeDetailActivity.launchAnimeDetail(
                        (Activity)mView.getContext(),
                        model.getAnimeId()+"")
        );
    }
}
