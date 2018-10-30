package com.xyoye.dandanplay.ui.weight.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.activities.DanmuNetworkActivity;
import com.xyoye.dandanplay.ui.activities.DownloadMangerActivity;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.ui.weight.dialog.DialogUtils;
import com.xyoye.dandanplay.utils.Config;
import com.xyoye.dandanplay.utils.FileUtils;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import libtorrent.File;
import libtorrent.Libtorrent;

/**
 * Created by YE on 2018/10/27.
 */

public class DownloadManagerItem implements AdapterItem<Torrent> {
    private long DAY = 24 * 60 * 60 * 1000;

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
    @BindView(R.id.play_action_ll)
    LinearLayout playActionLl;
    @BindView(R.id.bind_danmu_action_ll)
    LinearLayout bindDanmuActionLl;
    @BindView(R.id.info_action_ll)
    LinearLayout infoActionLl;
    @BindView(R.id.delete_action_ll)
    LinearLayout deleteActionLl;
    @BindView(R.id.close_action_ll)
    LinearLayout closeActionLl;
    @BindView(R.id.torrent_action_ll)
    LinearLayout torrentActionLl;
    @BindView(R.id.bind_danmu_iv)
    ImageView bindDanmuIv;
    @BindView(R.id.bind_danmu_tv)
    TextView bindDanmuTv;


    private Context context;
    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_download_torrent;
    }

    @Override
    public void initItemViews(View itemView) {
        context = itemView.getContext();
        mView = itemView;
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
            downloadSpeedTv.setText(getSpeed(torrent));
            downloadDurationTv.setText(duration);
            setStatus(torrent);
        }

        if (!StringUtils.isEmpty(torrent.getDanmuPath())) {
            bindDanmuIv.setImageResource(R.mipmap.ic_download_binded_danmu);
            bindDanmuTv.setText("已绑定");
            bindDanmuTv.setTextColor(context.getResources().getColor(R.color.theme_color));
        }else {
            bindDanmuIv.setImageResource(R.mipmap.ic_download_bind_danmu);
            bindDanmuTv.setText("弹幕");
            bindDanmuTv.setTextColor(context.getResources().getColor(R.color.white));
        }

        mView.setOnLongClickListener(v -> {
            torrentActionLl.setVisibility(View.VISIBLE);
            if (torrent.isDone()) playActionLl.setVisibility(View.VISIBLE);
            if (torrent.isDone() || (Libtorrent.torrentPendingBytesCompleted(torrent.getId()) > 16 * 1024 * 1024))
                bindDanmuActionLl.setVisibility(View.VISIBLE);
            deleteActionLl.setVisibility(View.VISIBLE);
            closeActionLl.setVisibility(View.VISIBLE);

            // TODO: 2018/10/30 下载详情未完善
            infoActionLl.setVisibility(View.GONE);
            return false;
        });

        downloadCtrlRl.setOnClickListener(v -> {
            setAction(torrent);
        });
        playActionLl.setOnClickListener(v -> {
            long l = Libtorrent.torrentFilesCount(torrent.getId());
            for (long i = 0; i < l; i++) {
                File playFile = Libtorrent.torrentFiles(torrent.getId(), i);
                if (playFile.getCheck()) {
                    String ext = com.blankj.utilcode.util.FileUtils.getFileExtension(playFile.getPath()).toUpperCase();
                    if (Config.videoType.contains(ext)) {
                        String videoTitle = playFile.getPath();
                        if (!torrent.getFolder().endsWith("/")) torrent.setFolder(torrent.getFolder() + "/");
                        String path = torrent.getFolder() + videoTitle;
                        String danmuPath = torrent.getDanmuPath();
                        int episodeId = torrent.getEpisodeId();

                        Intent intent = new Intent(context, PlayerActivity.class);
                        intent.putExtra("path", path);
                        intent.putExtra("title", videoTitle);
                        intent.putExtra("danmu_path", danmuPath);
                        intent.putExtra("current", 0);
                        intent.putExtra("episode_id", episodeId);
                        context.startActivity(intent);
                        return;
                    }
                }
            }
            ToastUtils.showShort("未找到可播放视频");
            torrentActionLl.setVisibility(View.GONE);
        });
        bindDanmuActionLl.setOnClickListener(v -> {
            long l = Libtorrent.torrentFilesCount(torrent.getId());
            for (long i = 0; i < l; i++) {
                File playFile = Libtorrent.torrentFiles(torrent.getId(), i);
                if (playFile.getCheck()) {
                    String ext = com.blankj.utilcode.util.FileUtils.getFileExtension(playFile.getPath()).toUpperCase();
                    if (Config.videoType.contains(ext)) {
                        String path = torrent.getFolder() + "/" + playFile.getPath();
                        Intent intent = new Intent(context, DanmuNetworkActivity.class);
                        intent.putExtra("path", path);
                        intent.putExtra("position", position);
                        Activity activity = (Activity) context;
                        activity.startActivityForResult(intent, DownloadMangerActivity.BIND_DANMU);
                        return;
                    }
                }
            }
            torrentActionLl.setVisibility(View.GONE);
        });
        infoActionLl.setOnClickListener(v -> {
            torrentActionLl.setVisibility(View.GONE);
        });
        deleteActionLl.setOnClickListener(v -> {
            new DialogUtils.Builder(context)
                    .setOkListener(dialog -> {
                        dialog.dismiss();
                        EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_DELETE_TASK, torrent));
                    })
                    .setExtraListener(dialog -> {
                        dialog.dismiss();
                        EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_DELETE_FILE, torrent));
                    })
                    .setCancelListener(DialogUtils::dismiss)
                    .build()
                    .show("删除任务和文件", "确认删除任务？", true, true);
            torrentActionLl.setVisibility(View.GONE);
        });
        closeActionLl.setOnClickListener(v -> torrentActionLl.setVisibility(View.GONE));

    }

    private String getSpeed(Torrent torrent) {
        if (torrent.isDone()) return "- · ↓ 0B/s · ↑ 0B/s";
        if (torrent.isError()) return "- · ↓ 0B/s · ↑ 0B/s";
        String str = "";
        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusQueued:
            case Libtorrent.StatusPaused:
                str += "- · ↓ 0B/s · ↑ 0B/s";
                break;
            case Libtorrent.StatusSeeding:
            case Libtorrent.StatusChecking:
            case Libtorrent.StatusDownloading:
                long c = 0;
                if (Libtorrent.metaTorrent(torrent.getId())) {
                    long p = Libtorrent.torrentPendingBytesLength(torrent.getId());
                    c = p - Libtorrent.torrentPendingBytesCompleted(torrent.getId());
                }
                int a = torrent.downloaded.getAverageSpeed();
                String left = "∞";
                if (c > 0 && a > 0) {
                    long diff = c * 1000 / a;
                    int diffDays = (int) (diff / (DAY));
                    if (diffDays < 30)
                        left = "" + TorrentUtil.formatDuration(context, diff) + "";
                }
                str += left;
                str += " · ↓ " + FileUtils.convertFileSize(torrent.downloaded.getCurrentSpeed()) + context.getString(R.string.per_second);
                str += " · ↑ " + FileUtils.convertFileSize(torrent.uploaded.getCurrentSpeed()) + context.getString(R.string.per_second);
                break;
        }

        return str.trim();
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
            return FileUtils.convertFileSize(torrent.getSize()) + "/" + FileUtils.convertFileSize(torrent.getSize());
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long size = Libtorrent.torrentPendingBytesLength(torrent.getId());
            long completed = Libtorrent.torrentPendingBytesCompleted(torrent.getId());
            return FileUtils.convertFileSize(completed) + "/" + FileUtils.convertFileSize(size);
        }
        return "未知/未知";
    }

    private void setStatus(Torrent torrent) {
        if (torrent.getStatus() == -1) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
            downloadStatusTv.setText("连接中");
            return;
        }

        if (torrent.isDone()) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.bilibili_pink));
            downloadStatusTv.setText("已完成");
            return;
        }

        if (torrent.isError()) {
            downloadStatusIv.setImageResource(R.mipmap.ic_download_error);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.ared));
            downloadStatusTv.setText("错误");
            return;
        }

        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusQueued:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("等待中");
                break;
            case Libtorrent.StatusChecking:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
                downloadStatusTv.setText("连接中");
                break;
            case Libtorrent.StatusPaused:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_pause);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("已暂停");
                break;
            case Libtorrent.StatusSeeding:
            case Libtorrent.StatusDownloading:
                downloadStatusIv.setImageResource(R.mipmap.ic_download_start);
                downloadStatusTv.setTextColor(context.getResources().getColor(R.color.theme_color));
                downloadStatusTv.setText("下载中");
                break;
        }
    }

    private void setAction(Torrent torrent) {
        if (torrent.isDone()) return;
        if (torrent.isError()) return;

        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusChecking:
            case Libtorrent.StatusSeeding:
            case Libtorrent.StatusDownloading:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_PAUSE, torrent));
                break;
            case Libtorrent.StatusPaused:
            case Libtorrent.StatusQueued:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_RESUME, torrent));
                break;
        }
    }
}
