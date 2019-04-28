package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.TrackerBean;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/4/28.
 */

public class TrackerItem implements AdapterItem<TrackerBean> {
    @BindView(R.id.item_layout)
    RelativeLayout relativeLayout;
    @BindView(R.id.tracker_tv)
    TextView trackerTv;
    @BindView(R.id.check_cb)
    CheckBox checkBox;

    private TrackerItemListener itemListener;

    public TrackerItem(TrackerItemListener itemListener){
        this.itemListener = itemListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_tracker;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(TrackerBean model, int position) {
        if (model.isSelectType()){
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(model.isSelected());

            relativeLayout.setOnLongClickListener(null);
            relativeLayout.setOnClickListener(v -> {
                if (itemListener != null){
                    itemListener.onClick(position, !model.isSelected());
                }
            });
        }else {
            checkBox.setVisibility(View.GONE);
            relativeLayout.setOnLongClickListener(v -> {
                if (itemListener != null){
                    itemListener.onLongClick(position);
                }
                return true;
            });
            relativeLayout.setOnClickListener(null);
        }
        trackerTv.setText(model.getTracker());
        checkBox.setClickable(false);
    }

    public interface TrackerItemListener{
        void onClick(int position, boolean isChecked);

        void onLongClick(int position);
    }
}
