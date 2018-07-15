package com.xyoye.dandanplay.ui.mainMod;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimaBeans;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/15.
 */


public class AnimaItem implements AdapterItem<AnimaBeans.BangumiListBean> {
    @BindView(R.id.image_iv)
    ImageView imageView;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anima;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(AnimaBeans.BangumiListBean model, int position) {

        statusTv.setText(model.isIsOnAir()
                         ? "连载中"
                         : "已完结");

        Glide.with(imageView.getContext()).load(model.getImageUrl()).into(imageView);
    }
}
