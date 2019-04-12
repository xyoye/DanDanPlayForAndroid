package com.xyoye.dandanplay.ui.weight.item;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.weight.dialog.TorrentDownloadDetailDialog;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import libtorrent.Libtorrent;

/**
 * Created by YE on 2018/10/27.
 */

public class DownloadManagerItem implements AdapterItem<Torrent> {

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

    @Override
    public void onUpdateViews(Torrent torrent, int position) {
        if (torrent.getStatus() == -1) {
            String speed = "∞ ↓ 0b/s · ↑ 0b/s";
            String duration = "未知/未知（" + 0 + "%）";
            downloadTitleTv.setText(torrent.getTitle());
            downloadSpeedTv.setText(speed);
            downloadDurationPb.setProgress(0);
            downloadDurationTv.setText(duration);
            downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
            downloadStatusTv.setText("等待中");
        } else {
            int progress = getProgress(torrent);
            String duration = getDuration(torrent) + "（" + progress + "%）";

            downloadTitleTv.setText(torrent.getTitle());
            downloadDurationPb.setProgress(progress);
            downloadSpeedTv.setText(TorrentUtil.getSpeed(context, torrent));
            downloadDurationTv.setText(duration);
            setStatus(torrent);
        }

        downloadInfoRl.setOnLongClickListener(v -> {
            new TorrentDownloadDetailDialog(context, position, statusStr).show();
            return true;
        });

        downloadInfoRl.setOnClickListener(v ->  new TorrentDownloadDetailDialog(context, position, statusStr).show());

        downloadCtrlRl.setOnClickListener(v -> changeStatus(torrent, position));
    }

    private int getProgress(Torrent torrent) {
        if (torrent.isDone()) return 100;
        if (torrent.isError()) return 0;
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long p = Libtorrent.torrentPendingBytesLength(torrent.getId());
            if (p == 0)
                return 0;
            return (int) (Libtorrent.torrentPendingBytesCompleted(torrent.getId()) * 100 / p);
        }
        return 0;
    }

    private String getDuration(Torrent torrent) {
        if (torrent.isError())
            return "未知/未知";
        if (torrent.isDone())
            return CommonUtils.convertFileSize(torrent.getSize()) + "/" + CommonUtils.convertFileSize(torrent.getSize());
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long size = Libtorrent.torrentPendingBytesLength(torrent.getId());
            long completed = Libtorrent.torrentPendingBytesCompleted(torrent.getId());
            return CommonUtils.convertFileSize(completed) + "/" + CommonUtils.convertFileSize(size);
        }
        return "未知/未知";
    }

    private void setStatus(Torrent torrent) {
        if (torrent.getStatus() == -1) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
            downloadStatusTv.setText("连接中");
            statusStr = "connecting";
            return;
        }

        if (torrent.isDone()) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.bilibili_pink));
            downloadStatusTv.setText("已完成");
            statusStr = "done";
            return;
        }

        if (torrent.isError()) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.ared));
            downloadStatusTv.setText("错误");
            statusStr = "error";
            return;
        }

        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusQueued:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("等待中");
                statusStr = "queued";
                break;
            case Libtorrent.StatusChecking:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("连接中");
                statusStr = "checking";
                break;
            case Libtorrent.StatusPaused:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_pause);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("已暂停");
                statusStr = "paused";
                break;
            case Libtorrent.StatusSeeding:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("下载中");
                statusStr = "seeding";
                break;
            case Libtorrent.StatusDownloading:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("下载中");
                statusStr = "downloading";
                break;
        }
    }

    private void changeStatus(Torrent torrent, int position) {
        if (torrent.isDone()) return;
        if (torrent.isError()) return;

        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusChecking:
            case Libtorrent.StatusSeeding:
            case Libtorrent.StatusDownloading:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_PAUSE, position));
                break;
            case Libtorrent.StatusPaused:
            case Libtorrent.StatusQueued:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_RESUME, position));
                break;
        }
    }
}
