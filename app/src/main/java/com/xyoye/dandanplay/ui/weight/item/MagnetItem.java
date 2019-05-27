package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/15.
 */

public class MagnetItem implements AdapterItem<MagnetBean.ResourcesBean> {
    @BindView(R.id.magnet_title_tv)
    TextView magnetTitleTv;
    @BindView(R.id.magnet_size_tv)
    TextView magnetSizeTv;
    @BindView(R.id.magnet_subgroup_tv)
    TextView magnetSubgroupTv;
    @BindView(R.id.magnet_type_tv)
    TextView magnetTypeTv;
    @BindView(R.id.magnet_time_tv)
    TextView magnetTimeTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_magnet;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(MagnetBean.ResourcesBean model, int position) {
        magnetTitleTv.setText(model.getTitle());
        magnetSizeTv.setText(model.getFileSize());
        magnetSubgroupTv.setText(model.getSubgroupName());
        magnetTypeTv.setText(model.getTypeName());
        magnetTimeTv.setText(model.getPublishDate());

        mView.setOnClickListener(v -> {
            EventBus.getDefault().post(model);
        });
    }
}
