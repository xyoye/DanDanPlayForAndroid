package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.TorrentCheckBean;
import com.xyoye.dandanplay.ui.weight.item.TorrentFileCheckItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

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

    private List<TorrentCheckBean> torrentFileList;
    private TorrentInfo torrentInfo;
    private OnTorrentSelectedListener listener;

    public TorrentFileCheckDialog(@NonNull Context context, TorrentInfo torrentInfo, OnTorrentSelectedListener listener) {
        super(context, R.style.Dialog);
        this.torrentInfo = torrentInfo;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_torrent_file_check);
        ButterKnife.bind(this, this);

        torrentFileList = new ArrayList<>();

        for (int i = 0; i < torrentInfo.numFiles(); i++) {
            TorrentCheckBean torrentCheckBean = new TorrentCheckBean();
            torrentCheckBean.setChecked(true);
            torrentCheckBean.setName(torrentInfo.files().fileName(i));
            torrentCheckBean.setLength(torrentInfo.files().fileSize(i));
            torrentFileList.add(torrentCheckBean);
        }

        fileRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        BaseRvAdapter<TorrentCheckBean> checkAdapter = new BaseRvAdapter<TorrentCheckBean>(torrentFileList) {
            @NonNull
            @Override
            public AdapterItem<TorrentCheckBean> onCreateItem(int viewType) {
                return new TorrentFileCheckItem((position, isChecked) ->
                        torrentFileList.get(position).setChecked(isChecked));
            }
        };
        fileRv.setAdapter(checkAdapter);

        cancelTv.setOnClickListener(v -> TorrentFileCheckDialog.this.dismiss());

        confirmTv.setOnClickListener(v -> {
            if (listener != null){
                boolean containsChecked = false;
                Boolean[] checkedIndexes = new Boolean[torrentFileList.size()];
                for (int i = 0; i < torrentFileList.size(); i++) {
                    if (!containsChecked){
                        containsChecked = torrentFileList.get(i).isChecked();
                    }
                    checkedIndexes[i] = torrentFileList.get(i).isChecked();
                }
                if (containsChecked){
                    listener.onSelected(checkedIndexes);
                    TorrentFileCheckDialog.this.dismiss();
                }else {
                    ToastUtils.showShort("请至少选择一个下载文件");
                }
            }
        });
    }

    public interface OnTorrentSelectedListener{
        void onSelected(Boolean[] checkedIndexes);
    }
}
