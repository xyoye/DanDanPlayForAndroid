package com.xyoye.dandanplay.ui.personalMod;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.ui.animeMod.AnimeDetailActivity;
import com.xyoye.dandanplay.weight.CornersCenterCrop;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */


public class PersonalFavoriteAnimaItem implements AdapterItem<AnimeFavoriteBean.FavoritesBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.anima_title)
    TextView animaTitle;
    @BindView(R.id.status_tv)
    TextView statusTv;

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
                .transform(new CornersCenterCrop(10));

        Glide.with(imageView.getContext())
                .load(model.getImageUrl())
                .apply(options)
                .into(imageView);

        animaTitle.setText(model.getAnimeTitle());

        String status = "进度:"+model.getEpisodeWatched()+"话(全"+model.getEpisodeTotal()+"话)";
        statusTv.setText(status);

        mView.setOnClickListener(v ->{
            Intent intent = new Intent(mView.getContext(), AnimeDetailActivity.class);
            intent.putExtra("animaId", model.getAnimeId()+"");
            mView.getContext().startActivity(intent);
        });
    }
}
