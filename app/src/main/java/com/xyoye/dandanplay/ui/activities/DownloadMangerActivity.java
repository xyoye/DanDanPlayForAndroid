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
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.mvp.impl.DownloadManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.DownloadManagerItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentEvent;
import com.xyoye.dandanplay.utils.torrent.TorrentUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import libtorrent.Libtorrent;

/**
 * Created by YE on 2018/10/27.
 */

public class DownloadMangerActivity extends BaseMvpActivity<DownloadManagerPresenter> implements DownloadManagerView {
    public final static int BIND_DANMU = 1001;
    @BindView(R.id.download_rv)
    RecyclerView downloadRv;

    private BaseRvAdapter<Torrent> adapter;

    Handler mHandler = new Handler(Looper.getMainLooper()){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0) {
                for (int i=0; i<IApplication.torrentList.size(); i++){
                    Torrent torrent = IApplication.torrentList.get(i);
                    if (torrent.isDone()) continue;
                    if (torrent.isUpdate()){
                        adapter.notifyItemChanged(i);
                        if (Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusPaused){
                            torrent.setUpdate(false);
                        }
                    }

                }
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
        ((SimpleItemAnimator)downloadRv.getItemAnimator()).setSupportsChangeAnimations(false);
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
        String torrentMagnet = getIntent().getStringExtra("torrent_magnet");
        if (!StringUtils.isEmpty(torrentPath)) {
            Torrent torrent = new Torrent();
            torrent.setPath(torrentPath);
            torrent.setFolder(animeFolder+"/");
            torrent.setMagnet(torrentMagnet);
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
        showLoadingDialog("正在开启下载服务");
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

        boolean downloading = false;
        for (Torrent torrent : IApplication.torrentList) {
            if (torrent.isDone()) continue;
            if (Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusDownloading ||
                    Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusSeeding) {
                downloading = true;
                break;
            }
        }
        if (!downloading){
            if (ServiceUtils.isServiceRunning(TorrentService.class))
                ServiceUtils.stopService(TorrentService.class);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.all_start:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_START, null));
                break;
            case R.id.all_pause:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_PAUSE, null));
                break;
            case R.id.all_delete:
                new CommonDialog.Builder(this)
                        .showExtra()
                        .setAutoDismiss()
                        .setOkListener(dialog ->
                                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_DELETE_TASK, null)))
                        .setExtraListener(dialog ->
                                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_DELETE_FILE, null)))
                        .build()
                        .show("确认删除所有任务？","删除任务和文件");
                break;
            case R.id.tracker_manager:
                startActivity(new Intent(DownloadMangerActivity.this, TrackerActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == DownloadMangerActivity.BIND_DANMU){
                int episodeId = data.getIntExtra("episode_id", -1);
                String path = data.getStringExtra("path");
                int position = data.getIntExtra("position", -1);
                if (position != -1){
                    Torrent torrent = IApplication.torrentList.get(position);
                    torrent.setDanmuPath(path);
                    torrent.setEpisodeId(episodeId);
                    adapter.notifyItemChanged(position);
                    TorrentUtil.updateTorrent(torrent);
                    ToastUtils.showShort("绑定弹幕成功");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(MessageEvent event){
        if (event.getMsg() == MessageEvent.UPDATE_DOWNLOAD_MANAGER)
            adapter.notifyDataSetChanged();
    }
}
