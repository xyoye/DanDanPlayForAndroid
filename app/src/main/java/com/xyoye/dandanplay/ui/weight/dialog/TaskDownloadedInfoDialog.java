package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadedInfoDialog extends Dialog {
    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.magnet_tv)
    TextView magnetTv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private Context context;
    private int taskPosition;
    private DownloadedTaskBean taskBean;
    private TaskDeleteListener taskDeleteListener;

    public TaskDownloadedInfoDialog(@NonNull Context context, int taskPosition, DownloadedTaskBean taskBean, TaskDeleteListener taskDeleteListener) {
        super(context, R.style.Dialog);
        this.taskPosition = taskPosition;
        this.context = context;
        this.taskBean = taskBean;
        this.taskDeleteListener = taskDeleteListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_downloaded_task_info);
        ButterKnife.bind(this, this);

        nameTv.setText(taskBean.getTitle());
        pathTv.setText(taskBean.getSaveDirPath());
        magnetTv.setText(taskBean.getMagnet());
        statusTv.setText("已完成");
    }

    @OnClick({R.id.close_tv, R.id.path_tv, R.id.magnet_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.close_tv:
                TaskDownloadedInfoDialog.this.dismiss();
                break;
            case R.id.path_tv:
                String path = taskBean.getSaveDirPath();
                ClipboardManager clipboardManagerPath = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataPath = ClipData.newPlainText("Label", path);
                if (clipboardManagerPath != null) {
                    clipboardManagerPath.setPrimaryClip(mClipDataPath);
                    ToastUtils.showShort("已复制路径：" + path);
                }
                break;
            case R.id.magnet_tv:
                String magnet = taskBean.getMagnet();
                ClipboardManager clipboardManagerMagnet = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataMagnet = ClipData.newPlainText("Label", magnet);
                if (clipboardManagerMagnet != null) {
                    clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                    ToastUtils.showShort("已复制Magnet：" + magnet);
                }
                break;
            case R.id.delete_tv:
                new CommonDialog.Builder(context)
                        .setAutoDismiss()
                        .showExtra()
                        .setOkListener(dialog -> {
                            taskDeleteListener.onTaskDelete(taskPosition, taskBean.getTorrentHash(), false);
                            TaskDownloadedInfoDialog.this.dismiss();
                        })

                        .setExtraListener(dialog -> {
                            taskDeleteListener.onTaskDelete(taskPosition, taskBean.getTorrentHash(), true);
                            TaskDownloadedInfoDialog.this.dismiss();
                        })
                        .build()
                        .show("确认删除任务？", "删除任务和文件");
                break;
        }

    }

    public interface TaskDeleteListener {
        void onTaskDelete(int position, String taskHash, boolean withFile);
    }
}
