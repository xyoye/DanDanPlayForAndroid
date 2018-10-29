package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.mvp.impl.DownloadManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.weight.item.DownloadManagerItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/10/27.
 */

public class DownloadMangerActivity extends BaseActivity<DownloadManagerPresenter> implements DownloadManagerView {
    @BindView(R.id.download_rv)
    RecyclerView downloadRv;

    private BaseRvAdapter<Torrent> adapter;

    Handler mHandler = new Handler(Looper.getMainLooper()){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0) {
                adapter.notifyDataSetChanged();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0),1000);
            }
        }
    };

    @Override
    public void initView() {
        setTitle("下载管理");
        downloadRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        downloadRv.setNestedScrollingEnabled(false);
        downloadRv.setItemViewCacheSize(10);

        presenter.getTorrentList();
        adapter = new BaseRvAdapter<Torrent>(IApplication.torrentList) {
            @NonNull
            @Override
            public AdapterItem<Torrent> onCreateItem(int viewType) {
                return new DownloadManagerItem();
            }
        };
        downloadRv.setAdapter(adapter);

        if(ServiceUtils.isServiceRunning(TorrentService.class)){
            startNewTask();
        }else {
            startTorrentService();
            presenter.observeService();
        }
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected DownloadManagerPresenter initPresenter() {
        return new DownloadManagerPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_download_manager;
    }

    @Override
    public void refreshAdapter(List<Torrent> torrentList) {

    }
    @Override
    public void startNewTask(){
        String torrentPath = getIntent().getStringExtra("torrent_path");
        String animeFolder = getIntent().getStringExtra("anime_folder");
        if (!StringUtils.isEmpty(torrentPath)) {
            Torrent torrent = new Torrent();
            torrent.setPath(torrentPath);
            torrent.setFolder(animeFolder);
            EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_START, torrent));
        }
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void startTorrentService() {
        Intent intent = new Intent(this, TorrentService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else {
            startService(intent);
        }
    }

    @Override
    public void showLoading() {
        showLoadingDialog("正在开启下载服务", false);
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
    }
}
