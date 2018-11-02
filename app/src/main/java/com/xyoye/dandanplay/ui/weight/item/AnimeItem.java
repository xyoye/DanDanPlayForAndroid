package com.xyoye.dandanplay.ui.weight.item;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.PixelUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.ui.activities.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.utils.UserInfoShare;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/15.
 */


public class AnimeItem implements AdapterItem<AnimeBeans.BangumiListBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.anima_title)
    TextView animaTitle;
    @BindView(R.id.status_tv)
    TextView statusTv;
    @BindView(R.id.favorite_tv)
    TextView favoriteTv;

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
    public void onUpdateViews(AnimeBeans.BangumiListBean model, int position) {

        if (UserInfoShare.getInstance().isLogin()){
            favoriteTv.setVisibility(View.VISIBLE);
            if (model.isIsFavorited())
                favoriteTv.setText("已关注");
        }

        statusTv.setText(model.isIsOnAir()
                         ? "连载中"
                         : "已完结");

        animaTitle.setText(model.getAnimeTitle());

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(PixelUtil.dip2px(mView.getContext(), 5)));

        Glide.with(imageView.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .into(imageView);

        mView.setOnClickListener(v ->{
            Intent intent = new Intent(mView.getContext(), AnimeDetailActivity.class);
            intent.putExtra("animaId", model.getAnimeId()+"");
            mView.getContext().startActivity(intent);
        });
    }
}
