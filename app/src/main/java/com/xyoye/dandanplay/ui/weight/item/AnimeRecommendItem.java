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
import com.xyoye.dandanplay.ui.activities.AnimeDetailActivityV2;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/1/12.
 */

public class AnimeRecommendItem implements AdapterItem<AnimeBean> {
    @BindView(R.id.image_iv)
    ImageView imageIv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.type_tv)
    TextView typeTv;
    @BindView(R.id.rating_tv)
    TextView ratingTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime_recommend_v2;
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

        typeTv.setText(model.isIsOnAir() ? "连载中" : "已完结");

        ratingTv.setText(model.getRating()+"");

        mView.setOnClickListener(v ->
                AnimeDetailActivityV2.launchAnimeDetail(
                        (Activity)mView.getContext(),
                        model.getAnimeId()+"")
        );
    }
}
