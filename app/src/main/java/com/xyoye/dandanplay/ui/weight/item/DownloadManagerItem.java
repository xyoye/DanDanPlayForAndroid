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
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStatus;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/27.
 */

public class DownloadManagerItem implements AdapterItem<BtTask> {

    @BindView(R.id.download_title_tv)
    TextView downloadTitleTv;
    @BindView(R.id.download_duration_pb)
    ProgressBar downloadDurationPb;
    @BindView(R.id.download_speed_tv)
    TextView downloadSpeedTv;
    @BindView(R.id.download_duration_tv)
    TextView downloadDurationTv;
    @BindView(R.id.download_info_rl)
    RelativeLayout downloadInfoRl;
    @BindView(R.id.download_status_iv)
    ImageView downloadStatusIv;
    @BindView(R.id.download_status_tv)
    TextView downloadStatusTv;
    @BindView(R.id.download_ctrl_rl)
    RelativeLayout downloadCtrlRl;

    private Context context;
    private String statusStr;

    @Override
    public int getLayoutResId() {
        return R.layout.item_download_torrent;
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
    public void onUpdateViews(BtTask task, int position) {
        Torrent torrent = task.getTorrent();

        //checking和stopped不改变进度
        if (task.getTaskStatus() != TaskStatus.CHECKING &&
                task.getTaskStatus() != TaskStatus.STOPPED){

            //读取当前下载速度
            String speed = "↓ "+CommonUtils.convertFileSize(torrent.getDownloadRate()) + "/s";
            //读取当前下载进度
            int progress = getProgress(torrent);
            String duration = getDuration(torrent) + "（" + progress + "%）";

            downloadSpeedTv.setText(speed);
            downloadDurationPb.setProgress(progress);
            downloadDurationTv.setText(duration);
        }
        downloadTitleTv.setText(torrent.getTitle());

        //根据下载状态修改图标和文字
        checkStatus(task.getTaskStatus());

        //单击展示详情弹窗
        downloadInfoRl.setOnClickListener(v ->  new TaskDownloadingDetailDialog(context, position, statusStr).show());

        //点击图标切换任务状态
        downloadCtrlRl.setOnClickListener(v -> {
            if (task.isPaused()){
                task.resume();
            }else {
                task.pause();
            }
        });
    }

    private int getProgress(Torrent torrent) {
        if (torrent.isFinished()) return 100;
        if (torrent.isError()) return 0;
        if (torrent.getLength() == 0)
            return 0;
        return (int)((torrent.getDownloaded() * 100) / torrent.getLength());
    }

    private String getDuration(Torrent torrent) {
        if (torrent.isFinished())
            return CommonUtils.convertFileSize(torrent.getLength()) + "/" + CommonUtils.convertFileSize(torrent.getLength());
        if (torrent.isError())
            return "未知/未知";
        return CommonUtils.convertFileSize(torrent.getDownloaded()) + "/" + CommonUtils.convertFileSize(torrent.getLength());
    }

    private void checkStatus(TaskStatus taskStatus) {
        switch (taskStatus) {
            case FINISHED:
                downloadSpeedTv.setText("-- B/s");
                downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.bilibili_pink));
                downloadStatusTv.setText("已完成");
                statusStr = "done";
                break;
            case UNKNOWN:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.ared));
                downloadStatusTv.setText("未知");
                statusStr = "error";
                break;
            case ERROR:
                downloadSpeedTv.setText("↓ 0 B/s");
                downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.ared));
                downloadStatusTv.setText("错误");
                statusStr = "error";
                break;
            case CHECKING:
                downloadSpeedTv.setText("↓ 0 B/s");
                downloadDurationPb.setProgress(0);
                downloadDurationTv.setText("--");
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("连接中");
                statusStr = "connecting";
                break;
            case ALLOCATING:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("等待中");
                statusStr = "queued";
                break;
            case PAUSED:
                downloadSpeedTv.setText("↓ 0 B/s");
                downloadStatusIv.setImageResource(R.mipmap.ic_download_pause);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("已暂停");
                statusStr = "paused";
                break;
            case SEEDING:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("下载中");
                statusStr = "seeding";
                break;
            case DOWNLOADING:
            case DOWNLOADING_METADATA:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("下载中");
                statusStr = "downloading";
                break;
        }
    }
}
