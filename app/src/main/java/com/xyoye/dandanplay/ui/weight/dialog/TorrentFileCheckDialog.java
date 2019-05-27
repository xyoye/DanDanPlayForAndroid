package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.ui.weight.item.TorrentFileCheckItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xyoye on 2019/3/6.
 */

public class TorrentFileCheckDialog extends Dialog {

    @BindView(R.id.file_rv)
    RecyclerView fileRv;
    @BindView(R.id.cancel_tv)
    TextView cancelTv;
    @BindView(R.id.confirm_tv)
    TextView confirmTv;

    private BaseRvAdapter<Torrent.TorrentFile> fileAdapter;
    private Torrent torrent;
    private OnTorrentSelectedListener listener;

    public TorrentFileCheckDialog(@NonNull Context context, Torrent torrent, OnTorrentSelectedListener listener) {
        super(context, R.style.Dialog);
        this.torrent = torrent;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_torrent_file_check);
        ButterKnife.bind(this, this);

        fileRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        fileAdapter = new BaseRvAdapter<Torrent.TorrentFile>(torrent.getTorrentFileList()) {
            @NonNull
            @Override
            public AdapterItem<Torrent.TorrentFile> onCreateItem(int viewType) {
                return new TorrentFileCheckItem((position, isChecked) ->
                        torrent.getTorrentFileList().get(position).setCheck(isChecked));
            }
        };
        fileRv.setAdapter(fileAdapter);

        cancelTv.setOnClickListener(v -> TorrentFileCheckDialog.this.dismiss());

        confirmTv.setOnClickListener(v -> {
            if (listener != null){
                listener.onSelected(torrent);
                TorrentFileCheckDialog.this.dismiss();
            }
        });
    }

    public interface OnTorrentSelectedListener{
        void onSelected(Torrent torrent);
    }
}
