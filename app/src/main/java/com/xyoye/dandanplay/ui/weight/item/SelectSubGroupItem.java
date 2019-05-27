package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.bean.event.SelectInfoEvent;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/15.
 */

public class SelectSubGroupItem implements AdapterItem<SubGroupBean.SubgroupsBean> {

    @BindView(R.id.info_tv)
    TextView infoTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_select_info;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(SubGroupBean.SubgroupsBean model, int position) {
        infoTv.setText(model.getName());

        mView.setOnClickListener(v ->
                EventBus.getDefault().post(new SelectInfoEvent(SelectInfoEvent.SUBGROUP, model.getId(), model.getName())));
    }
}
