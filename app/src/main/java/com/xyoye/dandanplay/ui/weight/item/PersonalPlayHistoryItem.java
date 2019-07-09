package com.xyoye.dandanplay.ui.weight.item;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.ui.activities.AnimeDetailActivityV2;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class PersonalPlayHistoryItem implements AdapterItem<PlayHistoryBean.PlayHistoryAnimesBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.anima_title)
    TextView animaTitle;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private View mView;
    private Context context;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
        context = mView.getContext();
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(PlayHistoryBean.PlayHistoryAnimesBean model, int position) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(10));

        Glide.with(imageView.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .into(imageView);

        animaTitle.setText(model.getAnimeTitle());

        statusTv.setText(model.isIsOnAir()
                ? "连载中"
                : "已完结");

        mView.setOnClickListener(v ->
                AnimeDetailActivityV2.launchAnimeDetail(
                        (Activity)mView.getContext(),
                        model.getAnimeId()+"")
        );
    }
}
