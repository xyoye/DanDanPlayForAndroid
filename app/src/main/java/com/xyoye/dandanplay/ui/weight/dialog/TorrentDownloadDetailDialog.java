package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuEndEvent;
import com.xyoye.dandanplay.ui.weight.item.TorrentDetailDownloadFileItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2019/3/5.
 */

public class TorrentDownloadDetailDialog extends Dialog {
    public final static int BIND_DANMU = 1001;
    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.magnet_tv)
    TextView magnetTv;
    @BindView(R.id.file_rv)
    RecyclerView fileRv;

    private Torrent mTorrent;
    private BaseRvAdapter<Torrent.TorrentFile> fileAdapter;

    public TorrentDownloadDetailDialog(@NonNull Context context, int torrentPosition) {
        super(context, R.style.Dialog);
        this.mTorrent = IApplication.torrentList.get(torrentPosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_torrent_detail);
        ButterKnife.bind(this, this);

        String downloadPath = AppConfig.getInstance().getDownloadFolder();
        downloadPath = StringUtils.isEmpty(mTorrent.getAnimeTitle())
                ? downloadPath
                : downloadPath+mTorrent.getAnimeTitle();

        nameTv.setText(mTorrent.getTitle());
        pathTv.setText(downloadPath);
        magnetTv.setText(mTorrent.getMagnet());

        fileRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        fileAdapter = new BaseRvAdapter<Torrent.TorrentFile>(mTorrent.getTorrentFileList()) {
            @NonNull
            @Override
            public AdapterItem<Torrent.TorrentFile> onCreateItem(int viewType) {
                return new TorrentDetailDownloadFileItem();
            }
        };
        fileRv.setAdapter(fileAdapter);

    }

    @OnClick({R.id.dialog_cancel_iv, R.id.path_tv, R.id.magnet_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dialog_cancel_iv:
                TorrentDownloadDetailDialog.this.dismiss();
                break;
            case R.id.path_tv:
                String path = mTorrent.getPath();
                ClipboardManager clipboardManagerPath = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataPath = ClipData.newPlainText("Label", path);
                if (clipboardManagerPath != null) {
                    clipboardManagerPath.setPrimaryClip(mClipDataPath);
                    ToastUtils.showShort("已复制路径：" + path);
                }
                break;
            case R.id.magnet_tv:
                String magnet = mTorrent.getMagnet();
                ClipboardManager clipboardManagerMagnet = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataMagnet = ClipData.newPlainText("Label", magnet);
                if (clipboardManagerMagnet != null) {
                    clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                    ToastUtils.showShort("已复制Magnet：" + magnet);
                }
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDanmuBind(TorrentBindDanmuEndEvent event) {
        int position = event.getPosition();
        if (position < mTorrent.getTorrentFileList().size()) {
            Torrent.TorrentFile torrentFile = mTorrent.getTorrentFileList().get(position);
            torrentFile.setDanmuPath(event.getDanmuPath());
            torrentFile.setEpisodeId(event.getEpisodeId());
            fileAdapter.notifyItemChanged(position);
            ToastUtils.showShort("绑定弹幕成功");
        }
    }

    @Override
    public void show() {
        super.show();
        EventBus.getDefault().register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
    }
}
