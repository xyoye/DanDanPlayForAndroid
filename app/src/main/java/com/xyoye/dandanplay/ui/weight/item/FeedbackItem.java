package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FeedbackBean;
import com.xyoye.dandanplay.ui.weight.expandable.ExpandableLayout;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/7.
 */

public class FeedbackItem implements AdapterItem<FeedbackBean> {
    @BindView(R.id.expandable_layout)
    ExpandableLayout expandableLayout;
    @BindView(R.id.expandable_layout_header_tv)
    TextView headerTv;
    @BindView(R.id.expandable_layout_header_iv)
    ImageView tipsIv;
    @BindView(R.id.expandable_layout_content_tv)
    TextView contentTv;

    private View mView;

    public FeedbackItem() {
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_feedback;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(FeedbackBean model, int position) {
        headerTv.setText(model.getHeader());
        contentTv.setText(model.getContent());

        expandableLayout.setOnExpansionUpdateListener((expansionFraction, state) ->
                tipsIv.setRotation(expansionFraction * 90));

        mView.setOnClickListener(v -> expandableLayout.toggle());
    }
}
