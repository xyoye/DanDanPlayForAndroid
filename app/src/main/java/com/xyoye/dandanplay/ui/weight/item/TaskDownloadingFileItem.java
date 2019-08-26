package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.torrent.info.Torrent;
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivity;
import com.xyoye.dandanplay.ui.activities.play.DanmuNetworkActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

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

        danmuBindIv.setImageResource(StringUtils.isEmpty(model.getDanmuFilePath())
                ? R.mipmap.ic_danmu_unexists
                : R.mipmap.ic_danmu_exists);

        String fileName = FileUtils.getFileName(model.getFilePath());

        fileNameTv.setText(fileName);

        //文件是否忽略下载
        if (model.isChecked()) {
            fileNameTv.setTextColor(IApplication.get_resource().getColor(R.color.text_black));
            int progress = model.getFileLength() == 0
                    ? 0
                    : (int) (model.getFileDoneLength() * 100 / model.getFileLength());
            downloadDurationPb.setProgress(progress);

            String duration = CommonUtils.convertFileSize(model.getFileDoneLength()) + "/" + CommonUtils.convertFileSize(model.getFileLength());
            duration += "  (" + progress + "%)";
            durationTv.setText(duration);
        } else {
            fileNameTv.setTextColor(IApplication.get_resource().getColor(R.color.text_gray));
            downloadDurationPb.setProgress(0);
            durationTv.setText("已忽略");
        }

        mView.setOnClickListener(v -> {
//            if (CommonUtils.isMediaFile(fileName)) {
//                String httpUrl = "http://" + TorrentServer.TORRENT_IP + ":" + TorrentServer.TORRENT_PORT + model.getFilePath();
//                TorrentServer.setBtFilePath(model.getFilePath());
//
//                PlayerManagerActivity.launchPlayer(
//                        mView.getContext(),
//                        fileName,
//                        httpUrl,
//                        model.getDanmuFilePath(),
//                        0,
//                        model.getDanmuEpisodeId()
//                );
//            } else {
//                ToastUtils.showShort("不支持播放的文件格式");
//            }
        });

        danmuBindIv.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getFilePath())) {
                if (model.getFileDoneLength() > 16 * 1024 * 1024) {
                    Intent intent = new Intent(mView.getContext(), DanmuNetworkActivity.class);
                    intent.putExtra("task_hash", taskHash);
                    intent.putExtra("video_path", model.getFilePath());
                    intent.putExtra("task_file_position", position);
                    mActivity.startActivityForResult(intent, DownloadManagerActivity.TASK_DOWNLOADING_DANMU_BIND);
                } else {
                    ToastUtils.showShort("需下载16M后才能匹配弹幕");
                }
            } else {
                ToastUtils.showShort("不支持绑定弹幕的文件格式");
            }
        });
    }
}
