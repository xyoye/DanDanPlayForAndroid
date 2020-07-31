package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadedFileItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/9/10.
 */

public class TaskDownloadedFileDialog extends Dialog {
    @BindView(R.id.file_rv)
    RecyclerView fileRv;

    private Context context;

    private int taskPosition;
    private DownloadedTaskBean taskBean;
    private BaseRvAdapter<DownloadedTaskBean.DownloadedTaskFileBean> fileAdapter;

    public TaskDownloadedFileDialog(@NonNull Context context, int taskPosition, DownloadedTaskBean taskBean) {
        super(context, R.style.Dialog);

        this.context = context;
        this.taskPosition = taskPosition;
        this.taskBean = taskBean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_downloaded_task_file);
        ButterKnife.bind(this, this);

        List<DownloadedTaskBean.DownloadedTaskFileBean> fileBeanList = taskBean.getFileList();
        fileRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        fileAdapter = new BaseRvAdapter<DownloadedTaskBean.DownloadedTaskFileBean>(fileBeanList) {
            @NonNull
            @Override
            public AdapterItem<DownloadedTaskBean.DownloadedTaskFileBean> onCreateItem(int viewType) {
                return new TaskDownloadedFileItem(taskPosition, (Activity) context);
            }
        };
        fileRv.setAdapter(fileAdapter);
    }

    @OnClick(R.id.dialog_cancel_iv)
    public void onViewClicked() {
        TaskDownloadedFileDialog.this.dismiss();
    }

    public void updateFileList() {
        fileAdapter.notifyDataSetChanged();
    }
}
