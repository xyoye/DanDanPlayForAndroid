package com.xyoye.dandanplay.ui.weight.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.xunlei.downloadlib.parameter.TorrentInfo;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.TorrentCheckBean;
import com.xyoye.dandanplay.ui.weight.item.TorrentFileCheckItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/6/30.
 */

public class TorrentCheckPlayDialog extends AlertDialog {

    @BindView(R.id.file_rv)
    RecyclerView fileRv;

    private TorrentInfo torrentInfo;
    private List<TorrentCheckBean> torrentList;
    private OnSelectedListener listener;

    private BaseRvAdapter<TorrentCheckBean> checkAdapter;

    public TorrentCheckPlayDialog(@NonNull Context context, TorrentInfo torrentInfo, OnSelectedListener listener) {
        super(context, R.style.Dialog);
        this.torrentInfo = torrentInfo;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_torrent_check_play);
        ButterKnife.bind(this);

        torrentList = new ArrayList<>();

        for (TorrentFileInfo fileInfo : torrentInfo.mSubFileInfo) {
            TorrentCheckBean checkBean = new TorrentCheckBean();
            checkBean.setName(fileInfo.mFileName);
            checkBean.setLength(fileInfo.mFileSize);
            checkBean.setRealPlayIndex(fileInfo.mRealIndex);
            checkBean.setChecked(false);
            torrentList.add(checkBean);
        }

        //按大小排序
        Collections.sort(torrentList, (o1, o2) -> Long.compare(o2.getLength(), o1.getLength()));
        //默认选中第一个数据
        if (torrentList.size() > 0) {
            torrentList.get(0).setChecked(true);
        }

        fileRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        checkAdapter = new BaseRvAdapter<TorrentCheckBean>(torrentList) {
            @NonNull
            @Override
            public AdapterItem<TorrentCheckBean> onCreateItem(int viewType) {
                return new TorrentFileCheckItem(position -> {
                    LogUtils.e("hash: " + torrentInfo.mSubFileInfo[position].hash);
                    if (!CommonUtils.isMediaFile(torrentList.get(position).getName())) {
                        ToastUtils.showShort("不是可播放的视频文件");
                        return;
                    }
                    for (int i = 0; i < torrentList.size(); i++) {
                        boolean isChecked = torrentList.get(i).isChecked();
                        torrentList.get(i).setChecked(i == position);
                        if (isChecked != (i == position)) {
                            checkAdapter.notifyItemChanged(i);
                        }
                    }
                });
            }
        };
        fileRv.setAdapter(checkAdapter);
    }

    @OnClick({R.id.cancel_tv, R.id.sort_by_name_tv, R.id.sort_by_length_tv, R.id.play_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                TorrentCheckPlayDialog.this.dismiss();
                break;
            case R.id.sort_by_name_tv:
                Collections.sort(torrentList, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                checkAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_by_length_tv:
                Collections.sort(torrentList, (o1, o2) -> Long.compare(o2.getLength(), o1.getLength()));
                checkAdapter.notifyDataSetChanged();
                break;
            case R.id.play_tv:
                for (int i = 0; i < torrentList.size(); i++) {
                    if (torrentList.get(i).isChecked()) {
                        listener.onSelected(i, torrentList.get(i).getRealPlayIndex(), torrentList.get(i).getLength());
                        TorrentCheckPlayDialog.this.dismiss();
                        return;
                    }
                }
                ToastUtils.showShort("请至少选择一个播放文件");
                break;
        }
    }

    public interface OnSelectedListener {
        void onSelected(int position, int realIndex, long fileSize);
    }
}
