package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuStartEvent;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;

/**
 * Created by xyy on 2019/3/5.
 */

public class TorrentDetailDownloadFileItem implements AdapterItem<Torrent.TorrentFile> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;

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

        danmuBindIv.setImageResource(StringUtils.isEmpty(model.getDanmuPath())
                ? R.mipmap.ic_danmu_unexists
                : R.mipmap.ic_danmu_exists);

        fileNameTv.setText(model.getName());

        File realFile = new File(model.getPath());

        mView.setOnClickListener(v -> {
            if (realFile.length() >= model.getLength() && CommonUtils.isMediaFile(model.getName())) {
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
            if (CommonUtils.isMediaFile(model.getPath())){
                if (realFile.length() > 16 * 1024 * 1024){
                    EventBus.getDefault().post(new TorrentBindDanmuStartEvent(model.getPath(), position));
                }else {
                    ToastUtils.showShort("需下载16M后才能匹配弹幕");
                }
            }else {
                ToastUtils.showShort("不支持绑定弹幕的文件格式");
            }
        });
    }
}
