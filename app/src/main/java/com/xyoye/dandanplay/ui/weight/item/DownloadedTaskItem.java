package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedTaskItem implements AdapterItem<DownloadedTaskBean> {
    @BindView(R.id.task_title_tv)
    TextView taskTitleTv;
    @BindView(R.id.task_size_tv)
    TextView taskSizeTv;

    @Override
    public int getLayoutResId() {
        return R.layout.item_downloaded_task;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(DownloadedTaskBean model, int position) {
        taskTitleTv.setText(model.getTitle());
        taskSizeTv.setText(model.getTotalSize());
    }
}
