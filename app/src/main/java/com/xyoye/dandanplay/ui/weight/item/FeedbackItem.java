package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FeedbackBean;
import com.xyoye.dandanplay.ui.weight.ExpandableLayout;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/7.
 */

public class FeedbackItem implements AdapterItem<FeedbackBean> {
    @BindView(R.id.expandable_layout)
    ExpandableLayout expandableLayout;

    private FeedbackItemClickListener listener;
    private View mView;

    public FeedbackItem(FeedbackItemClickListener listener) {
        this.listener = listener;
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

        TextView headerTv = expandableLayout.findViewById(R.id.expandable_layout_header_tv);
        TextView contentTv = expandableLayout.findViewById(R.id.expandable_layout_content_tv);
        ImageView tipsIv = expandableLayout.findViewById(R.id.expandable_layout_header_iv);

        headerTv.setText(model.getHeader());
        contentTv.setText(model.getContent());

        if (model.isOpen()){
            expandableLayout.show();
            tipsIv.setRotation(90);
        }else {
            expandableLayout.hide();
            tipsIv.setRotation(270);
        }

        mView.setOnClickListener(v -> listener.onClick(position));
    }

    public interface FeedbackItemClickListener{
        void onClick(int position);
    }
}
