package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.weight.dialog.TaskDownloadingDetailDialog;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.TaskManageListener;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStateBean;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentStateCode;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/27.
 */

public class TaskDownloadingItem implements AdapterItem<TaskStateBean> {

    @BindView(R.id.download_title_tv)
    TextView downloadTitleTv;
    @BindView(R.id.download_duration_pb)
    ProgressBar downloadDurationPb;
    @BindView(R.id.download_speed_tv)
    TextView downloadSpeedTv;
    @BindView(R.id.download_duration_tv)
    TextView downloadDurationTv;
    @BindView(R.id.download_status_iv)
    ImageView downloadStatusIv;
    @BindView(R.id.download_status_tv)
    TextView downloadStatusTv;
    @BindView(R.id.download_info_rl)
    RelativeLayout downloadInfoRl;
    @BindView(R.id.download_ctrl_rl)
    RelativeLayout downloadCtrlRl;

    private Context context;
    private TaskManageListener taskManageListener;

    public TaskDownloadingItem(TaskManageListener taskManageListener) {
        this.taskManageListener = taskManageListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_downloading_task;
    }

    @Override
    public void initItemViews(View itemView) {
        context = itemView.getContext();
    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateViews(TaskStateBean taskState, int position) {
        downloadTitleTv.setText(taskState.getTaskName());

        switch (taskState.getStateCode()) {
            case DOWNLOADING:
                String downloadSpeed = CommonUtils.convertFileSize(taskState.getDownloadSpeed());
                downloadSpeedTv.setText("↓ " + downloadSpeed + "/s");
                downloadDurationPb.setProgress(taskState.getProgress());
                downloadDurationTv.setText(getDuration(taskState));

                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                downloadStatusTv.setText("下载中");
                break;
            case PAUSED:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(taskState.getProgress());
                downloadDurationTv.setText(getDuration(taskState));

                downloadStatusIv.setImageResource(R.mipmap.ic_download_pause);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                downloadStatusTv.setText("已暂停");
                break;
            case STOPPED:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(taskState.getProgress());
                downloadDurationTv.setText(getDuration(taskState));

                downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_pink));
                downloadStatusTv.setText("已停止");
                break;
            case UNKNOWN:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(0);
                downloadDurationTv.setText("");

                downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_red));
                downloadStatusTv.setText("未知");
                break;
            case FINISHED:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(100);
                downloadDurationTv.setText(getDuration(taskState));

                downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_pink));
                downloadStatusTv.setText("已完成");
                break;
            case ERROR:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(0);
                downloadDurationTv.setText("");

                downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_red));
                downloadStatusTv.setText("错误");
                break;
            case CHECKING:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(0);
                downloadDurationTv.setText("");

                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                downloadStatusTv.setText("连接中");
                break;
            case ALLOCATING:
                downloadSpeedTv.setText("↓ --");
                downloadDurationPb.setProgress(taskState.getProgress());
                downloadDurationTv.setText(getDuration(taskState));

                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                downloadStatusTv.setText("等待中");
                break;
        }


        //单击展示详情弹窗
        downloadInfoRl.setOnClickListener(v -> {
            String statusStr = downloadStatusTv.getText().toString();
            new TaskDownloadingDetailDialog(context, taskState, statusStr, taskManageListener).show();
        });

        //点击图标切换任务状态
        downloadCtrlRl.setOnClickListener(v -> {
            if (taskState.getStateCode() == TorrentStateCode.PAUSED) {
                taskManageListener.resumeTask(taskState.getTorrentHash());
            } else {
                taskManageListener.pauseTask(taskState.getTorrentHash());
            }
        });
    }

    private String getDuration(TaskStateBean taskState) {
        return CommonUtils.convertFileSize(taskState.getReceivedBytes()) + "/" + CommonUtils.convertFileSize(taskState.getTotalBytes());
    }
}
