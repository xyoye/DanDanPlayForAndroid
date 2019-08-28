package com.xyoye.dandanplay.ui.weight.item;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeTagBean;
import com.xyoye.dandanplay.ui.activities.anime.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.ui.weight.SlantedTextView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/7/9.
 */

public class TagAnimeItem implements AdapterItem<AnimeTagBean.AnimesBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.anime_title)
    TextView anime_title;
    @BindView(R.id.follow_tag_view)
    SlantedTextView followTagView;

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
    public void onUpdateViews(AnimeTagBean.AnimesBean model, int position) {

        if (AppConfig.getInstance().isLogin()) {
            followTagView.setVisibility(model.isIsFavorited()
                    ? View.VISIBLE
                    : View.GONE);
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(3));

        Glide.with(imageView.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .into(imageView);

        anime_title.setText(model.getAnimeTitle());

        mView.setOnClickListener(v ->
                AnimeDetailActivity.launchAnimeDetail(
                        (Activity)mView.getContext(),
                        model.getAnimeId()+"")
        );
    }
}
