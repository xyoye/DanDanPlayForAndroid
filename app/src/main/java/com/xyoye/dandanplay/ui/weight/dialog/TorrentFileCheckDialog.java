package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.TorrentCheckBean;
import com.xyoye.dandanplay.ui.weight.item.TorrentFileCheckItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.libtorrent4j.Priority;
import org.libtorrent4j.TorrentInfo;

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

    private List<TorrentCheckBean> checkBeanList;
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

        checkBeanList = new ArrayList<>();

        for (int i = 0; i < torrentInfo.numFiles(); i++) {
            TorrentCheckBean torrentCheckBean = new TorrentCheckBean();
            torrentCheckBean.setChecked(true);
            torrentCheckBean.setName(torrentInfo.files().fileName(i));
            torrentCheckBean.setLength(torrentInfo.files().fileSize(i));
            checkBeanList.add(torrentCheckBean);
        }

        fileRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        BaseRvAdapter<TorrentCheckBean> checkAdapter = new BaseRvAdapter<TorrentCheckBean>(checkBeanList) {
            @NonNull
            @Override
            public AdapterItem<TorrentCheckBean> onCreateItem(int viewType) {
                return new TorrentFileCheckItem((position, isChecked) ->
                        checkBeanList.get(position).setChecked(isChecked));
            }
        };
        fileRv.setAdapter(checkAdapter);

        cancelTv.setOnClickListener(v -> TorrentFileCheckDialog.this.dismiss());

        confirmTv.setOnClickListener(v -> {
            if (listener != null) {
                boolean containsChecked = false;
                List<Priority> priorityList = new ArrayList<>();

                for (TorrentCheckBean checkBean : checkBeanList) {
                    if (!containsChecked) {
                        containsChecked = checkBean.isChecked();
                    }
                    priorityList.add(checkBean.isChecked()
                            ? Priority.DEFAULT
                            : Priority.IGNORE
                    );
                }
                
                if (containsChecked) {
                    listener.onSelected(priorityList);
                    TorrentFileCheckDialog.this.dismiss();
                } else {
                    ToastUtils.showShort("请至少选择一个下载文件");
                }
            }
        });
    }

    public interface OnTorrentSelectedListener {
        void onSelected(List<Priority> priorityList);
    }
}
