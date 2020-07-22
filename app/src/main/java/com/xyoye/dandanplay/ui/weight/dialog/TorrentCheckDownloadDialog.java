package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.frostwire.jlibtorrent.Priority;
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

public class TorrentCheckDownloadDialog extends Dialog {

    @BindView(R.id.file_rv)
    RecyclerView fileRv;
    @BindView(R.id.cancel_tv)
    TextView cancelTv;
    @BindView(R.id.download_tv)
    TextView downloadTv;
    @BindView(R.id.all_not_check_tv)
    TextView allNotCheckTv;
    @BindView(R.id.all_check_tv)
    TextView allCheckTv;

    private List<TorrentCheckBean> checkBeanList;
    private TorrentInfo torrentInfo;
    private OnTorrentSelectedListener listener;
    private BaseRvAdapter<TorrentCheckBean> checkAdapter;

    public TorrentCheckDownloadDialog(@NonNull Context context, TorrentInfo torrentInfo, OnTorrentSelectedListener listener) {
        super(context, R.style.Dialog);
        this.torrentInfo = torrentInfo;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_torrent_check_download);
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
        checkAdapter = new BaseRvAdapter<TorrentCheckBean>(checkBeanList) {
            @NonNull
            @Override
            public AdapterItem<TorrentCheckBean> onCreateItem(int viewType) {
                return new TorrentFileCheckItem(position -> {
                    boolean isChecked = checkBeanList.get(position).isChecked();
                    checkBeanList.get(position).setChecked(!isChecked);
                    checkAdapter.notifyItemChanged(position);
                });
            }
        };
        fileRv.setAdapter(checkAdapter);

        cancelTv.setOnClickListener(v -> TorrentCheckDownloadDialog.this.dismiss());

        downloadTv.setOnClickListener(v -> {
            if (listener != null) {
                boolean containsChecked = false;
                List<Priority> priorityList = new ArrayList<>();

                for (TorrentCheckBean checkBean : checkBeanList) {
                    if (!containsChecked) {
                        containsChecked = checkBean.isChecked();
                    }
                    priorityList.add(checkBean.isChecked()
                            ? Priority.NORMAL
                            : Priority.IGNORE
                    );
                }

                if (containsChecked) {
                    listener.onDownload(priorityList);
                    TorrentCheckDownloadDialog.this.dismiss();
                } else {
                    ToastUtils.showShort("请至少选择一个下载文件");
                }
            }
        });

        allCheckTv.setOnClickListener(v -> {
            for (TorrentCheckBean checkBean : checkBeanList) {
                checkBean.setChecked(true);
            }
            checkAdapter.notifyDataSetChanged();
        });

        allNotCheckTv.setOnClickListener(v -> {
            for (TorrentCheckBean checkBean : checkBeanList) {
                checkBean.setChecked(false);
            }
            checkAdapter.notifyDataSetChanged();
        });
    }

    public interface OnTorrentSelectedListener {
        void onDownload(List<Priority> priorityList);
    }
}
