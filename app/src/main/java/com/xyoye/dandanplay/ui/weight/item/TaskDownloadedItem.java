package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.ui.weight.dialog.TaskDownloadedDetailDialog;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class TaskDownloadedItem implements AdapterItem<DownloadedTaskBean> {
    @BindView(R.id.task_title_tv)
    TextView taskTitleTv;
    @BindView(R.id.task_size_tv)
    TextView taskSizeTv;
    @BindView(R.id.task_complete_time_tv)
    TextView taskCompleteTimeTv;

    private View mView;
    private TaskDownloadedDetailDialog.TaskDeleteListener taskDeleteListener;

    public TaskDownloadedItem(TaskDownloadedDetailDialog.TaskDeleteListener taskDeleteListener){
        this.taskDeleteListener = taskDeleteListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_downloaded_task;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(DownloadedTaskBean model, int position) {
        taskTitleTv.setText(model.getTitle());
        taskSizeTv.setText(model.getTotalSize());
        taskCompleteTimeTv.setText(model.getCompleteTime());

        mView.setOnClickListener(v -> new TaskDownloadedDetailDialog(mView.getContext(), position, model, taskDeleteListener).show());
    }
}
