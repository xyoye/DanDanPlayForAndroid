package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuStartEvent;
import com.xyoye.dandanplay.ui.activities.DanmuNetworkActivity;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.ui.weight.dialog.TorrentDownloadDetailDialog;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.StringUtil;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyy on 2019/3/5.
 */

public class TorrentDetailDownloadFileItem implements AdapterItem<Torrent.TorrentFile> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.progress_tv)
    TextView progressTv;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;
    @BindView(R.id.download_status_iv)
    ImageView downloadStatusIv;

    private View mView;

    public TorrentDetailDownloadFileItem() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_torrent_detail_download_file;
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
        if (model.getLength() != 0) {
            if (model.isDone()) {
                if (CommonUtils.isMediaFile(model.getName())){
                    downloadStatusIv.setImageResource(R.mipmap.ic_download_pause);
                }else {
                    downloadStatusIv.setImageResource(R.mipmap.ic_download_over);
                }
                progressTv.setTextColor(mView.getContext().getResources().getColor(R.color.theme_color));
                progressTv.setText("100%");
            }else {
                float pro = (float) model.getCompleted() / (float) model.getLength();
                String progress = (int) (pro * 100) + "%";

                downloadStatusIv.setImageResource(R.mipmap.ic_downloading);
                progressTv.setTextColor(mView.getContext().getResources().getColor(R.color.text_gray));
                progressTv.setText(progress);
            }
        }

        danmuBindIv.setImageResource(StringUtils.isEmpty(model.getDanmuPath())
                ? R.mipmap.ic_danmu_unexists
                : R.mipmap.ic_danmu_exists);

        fileNameTv.setText(model.getName());

        mView.setOnClickListener(v -> {
            if (model.getCompleted() == model.getLength() && CommonUtils.isMediaFile(model.getName())) {
                Intent intent = new Intent(mView.getContext(), PlayerActivity.class);
                intent.putExtra("path", model.getPath());
                intent.putExtra("title", model.getName());
                intent.putExtra("danmu_path", model.getDanmuPath());
                intent.putExtra("current", 0);
                intent.putExtra("episode_id", model.getEpisodeId());
                mView.getContext().startActivity(intent);
            }
        });

        danmuBindIv.setOnClickListener(v -> {
            if (model.isDone() || (model.getCompleted() > 16 * 1024 * 1024)){
                EventBus.getDefault().post(new TorrentBindDanmuStartEvent(model.getPath(), position));
            }else {
                ToastUtils.showShort("需下载16M后才能匹配弹幕");
            }
        });
    }
}
