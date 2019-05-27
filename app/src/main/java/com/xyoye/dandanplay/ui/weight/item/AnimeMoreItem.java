package com.xyoye.dandanplay.ui.weight.item;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.ui.activities.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/1/12.
 */

public class AnimeMoreItem implements AdapterItem<AnimeBean> {
    @BindView(R.id.image_iv)
    ImageView imageIv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.follow_tv)
    TextView followTv;
    @BindView(R.id.air_tv)
    TextView airTv;
    @BindView(R.id.type_tv)
    TextView typeTv;
    @BindView(R.id.restricted_tv)
    TextView restrictedTv;
    @BindView(R.id.rating_tv)
    TextView ratingTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime_more;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(AnimeBean model, int position) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(ConvertUtils.dp2px(5)));

        Glide.with(imageIv.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .into(imageIv);

        titleTv.setText(model.getAnimeTitle());

        followTv.setVisibility(model.isIsFavorited() ? View.VISIBLE : View.GONE);

        airTv.setText(model.isIsOnAir() ? "连载中" : "已完结");

        typeTv.setVisibility(View.GONE);

        restrictedTv.setVisibility(model.isIsRestricted() ? View.VISIBLE : View.GONE);

        ratingTv.setText(model.getRating() + "分");

        mView.setOnClickListener(v ->
                AnimeDetailActivity.launchAnimeDetail(
                (Activity)mView.getContext(),
                model.getAnimeId()+"")
        );
    }
}
