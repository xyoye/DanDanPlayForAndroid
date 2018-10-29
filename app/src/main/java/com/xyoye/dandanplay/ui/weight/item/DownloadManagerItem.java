package com.xyoye.dandanplay.ui.weight.item;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.utils.FileUtils;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
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
        if (torrent.getStatus() == -1){
            String speed = "∞ ↓ 0b/s · ↑ 0b/s";
            String duration = "未知/未知（"+0+"%）";
            downloadTitleTv.setText(torrent.getTitle());
            downloadSpeedTv.setText(speed);
            downloadDurationPb.setProgress(0);
            downloadDurationTv.setText(duration);
            downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
            downloadStatusTv.setText("等待中");
        }else {
            int progress = getProgress(torrent);
            String duration = getDuration(torrent)+"（"+progress+"%）";

            downloadTitleTv.setText(torrent.getTitle());
            downloadDurationPb.setProgress(progress);
            downloadSpeedTv.setText(getSpeed(torrent));
            downloadDurationTv.setText(duration);
            setStatus(torrent);
        }

        mView.setOnClickListener(v -> {
//            libtorrent.File playFile = null;
//            long l = Libtorrent.torrentFilesCount(torrent.getId());
//            System.out.println("download count："+l);
//            for (long i = 0; i < l; i++) {
//                playFile = Libtorrent.torrentFiles(torrent.getId(), i);
//                if (playFile.getCheck())
//                    break;
//            }
//            if (playFile == null) return;
//            File realFile = new File(torrent.getFolder(), playFile.getPath());
//            Uri uri = Uri.fromFile(realFile);
//            TLog.e(uri.toString());
//            //Uri uri = FileProvider.getUriForFile(context, context.getPackageName()+".fileProvider", realFile);
//            Intent intent = new Intent(context, PlayerActivity.class);
//            intent.setData(uri);
//            intent.setAction(Intent.ACTION_VIEW);
//            context.startActivity(intent);
        });

        downloadCtrlRl.setOnClickListener(v -> {
            setAction(torrent);
        });
    }

    private String getSpeed(Torrent torrent) {
        if (torrent.isDone()) return "- · ↓ 0B/s · ↑ 0B/s";
        String str = "";
        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusQueued:
            case Libtorrent.StatusChecking:
            case Libtorrent.StatusPaused:
                str += "- · ↓ 0B/s · ↑ 0B/s";
                break;
            case Libtorrent.StatusSeeding:
                str += "∞ · ↓ " + FileUtils.convertFileSize(torrent.downloaded.getCurrentSpeed()) + context.getString(R.string.per_second);
                str += " · ↑ " + FileUtils.convertFileSize(torrent.uploaded.getCurrentSpeed()) + context.getString(R.string.per_second);
                break;
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
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long p = Libtorrent.torrentPendingBytesLength(torrent.getId());
            if (p == 0)
                return 0;
            return (int) (Libtorrent.torrentPendingBytesCompleted(torrent.getId()) * 100 / p);
        }
        return 0;
    }

    private String getDuration(Torrent torrent){
        if (torrent.isDone()) return FileUtils.convertFileSize(torrent.getSize())+"/"+FileUtils.convertFileSize(torrent.getSize());
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long size = Libtorrent.torrentPendingBytesLength(torrent.getId());
            long completed = Libtorrent.torrentPendingBytesCompleted(torrent.getId());
            return FileUtils.convertFileSize(completed) +"/" + FileUtils.convertFileSize(size);
        }
        return "未知/未知";
    }

    private void setStatus(Torrent torrent){
        if (torrent.getStatus() == -1){
            downloadStatusIv.setImageResource(R.mipmap.ic_download_wait);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.text_gray));
            downloadStatusTv.setText("连接中");
            return;
        }

        if (torrent.isDone()){
            downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
            downloadStatusTv.setTextColor(context.getResources().getColor(R.color.bilibili_pink));
            downloadStatusTv.setText("已完成");
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

    private void setAction(Torrent torrent){
        if (torrent.isDone())return;

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
