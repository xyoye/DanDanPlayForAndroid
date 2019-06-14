package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuStartEvent;
import com.xyoye.dandanplay.ui.activities.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;
import com.xyoye.dandanplay.utils.smb.TorrentServer;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TorrentDetailDownloadFileItem implements AdapterItem<Torrent.TorrentFile> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.download_duration_pb)
    ProgressBar downloadDurationPb;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;

    private View mView;

    public TorrentDetailDownloadFileItem() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_torrent_detail_download_file_v2;
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
            fileNameTv.setTextColor(mView.getResources().getColor(R.color.text_black));
            int progress = (int)(model.getDownloaded() * 100 / model.getLength());
            downloadDurationPb.setProgress(progress);

            String duration = CommonUtils.convertFileSize(model.getDownloaded()) + "/" + CommonUtils.convertFileSize(model.getLength());
            duration += "  ("+progress+"%)";
            durationTv.setText(duration);
        }else {
            fileNameTv.setTextColor(mView.getResources().getColor(R.color.text_gray));
            downloadDurationPb.setProgress(0);
            durationTv.setText("已忽略");
        }

        mView.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getName())){
                String httpUrl = "http://" + LocalIPUtil.IP + ":" + LocalIPUtil.PORT + "/";
                TorrentServer.playFilePath = model.getPath();
                PlayerManagerActivity.launchPlayer(
                        mView.getContext(),
                        model.getName(),
                        httpUrl,
                        "",
                        0,
                        0
                );
//                if (model.getDownloaded() >= model.getLength()) {
//                    PlayerManagerActivity.launchPlayer(
//                            mView.getContext(),
//                            model.getName(),
//                            model.getPath(),
//                            model.getDanmuPath(),
//                            0,
//                            model.getEpisodeId());
//                }else {
//                    ToastUtils.showShort("文件尚未下载完成");
//                }
            }else {
                ToastUtils.showShort("不支持播放的文件格式");
            }

        });

        danmuBindIv.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getPath())) {
                if (model.getDownloaded() > 16 * 1024 * 1024) {
                    EventBus.getDefault().post(new TorrentBindDanmuStartEvent(model.getPath(), position));
                } else {
                    ToastUtils.showShort("需下载16M后才能匹配弹幕");
                }
            } else {
                ToastUtils.showShort("不支持绑定弹幕的文件格式");
            }
        });
    }
}
