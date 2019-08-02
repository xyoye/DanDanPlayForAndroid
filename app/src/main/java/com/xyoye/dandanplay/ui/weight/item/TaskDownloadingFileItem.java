package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuStartEvent;
import com.xyoye.dandanplay.ui.activities.DanmuNetworkActivity;
import com.xyoye.dandanplay.ui.activities.DownloadManagerActivityV2;
import com.xyoye.dandanplay.ui.activities.DownloadMangerActivity;
import com.xyoye.dandanplay.ui.activities.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.smbv2.TorrentServer;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadingFileItem implements AdapterItem<Torrent.TorrentFile> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.download_duration_pb)
    ProgressBar downloadDurationPb;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;

    private String taskHash;
    private Activity mActivity;
    private View mView;

    public TaskDownloadingFileItem(String hash, Activity activity) {
        this.taskHash = hash;
        mActivity = activity;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_task_downloading_file;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateViews(Torrent.TorrentFile model, int position) {

        danmuBindIv.setImageResource(StringUtils.isEmpty(model.getDanmuPath())
                ? R.mipmap.ic_danmu_unexists
                : R.mipmap.ic_danmu_exists);

        fileNameTv.setText(model.getName());

        //文件是否忽略下载
        if (model.isChecked()){
            fileNameTv.setTextColor(mActivity.getResources().getColor(R.color.text_black));
            int progress = model.getLength() == 0
                    ? 0
                    :(int)(model.getDownloaded() * 100 / model.getLength());
            downloadDurationPb.setProgress(progress);

            String duration = CommonUtils.convertFileSize(model.getDownloaded()) + "/" + CommonUtils.convertFileSize(model.getLength());
            duration += "  ("+progress+"%)";
            durationTv.setText(duration);
        }else {
            fileNameTv.setTextColor(mActivity.getResources().getColor(R.color.text_gray));
            downloadDurationPb.setProgress(0);
            durationTv.setText("已忽略");
        }

        mView.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getName())){
                String httpUrl = "http://" + TorrentServer.TORRENT_IP + ":" + TorrentServer.TORRENT_PORT + model.getPath();
                TorrentServer.setBtFilePath(model.getPath());

                PlayerManagerActivity.launchPlayer(
                        mView.getContext(),
                        model.getName(),
                        httpUrl,
                        model.getDanmuPath(),
                        0,
                        model.getEpisodeId()
                );
            }else {
                ToastUtils.showShort("不支持播放的文件格式");
            }
        });

        danmuBindIv.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getPath())) {
                if (model.getDownloaded() > 16 * 1024 * 1024) {
                    Intent intent = new Intent(mView.getContext(), DanmuNetworkActivity.class);
                    intent.putExtra("video_path", model.getPath());
                    intent.putExtra("task_hash", taskHash);
                    intent.putExtra("task_file_position", position);
                    mActivity.startActivityForResult(intent, DownloadManagerActivityV2.TASK_DOWNLOADING_DANMU_BIND);
                } else {
                    ToastUtils.showShort("需下载16M后才能匹配弹幕");
                }
            } else {
                ToastUtils.showShort("不支持绑定弹幕的文件格式");
            }
        });
    }
}
