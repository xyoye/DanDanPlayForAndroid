package com.xyoye.dandanplay.ui.personalMod;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.ui.animeMod.AnimeDetailActivity;
import com.xyoye.dandanplay.weight.CornersCenterCrop;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
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

        mView.setOnClickListener(v ->{
            Intent intent = new Intent(mView.getContext(), AnimeDetailActivity.class);
            intent.putExtra("animaId", model.getAnimeId()+"");
            mView.getContext().startActivity(intent);
        });
    }
}
