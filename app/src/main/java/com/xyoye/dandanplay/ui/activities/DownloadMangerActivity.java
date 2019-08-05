package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.bean.event.TaskBindDanmuEndEvent;
import com.xyoye.dandanplay.bean.event.TorrentBindDanmuStartEvent;
import com.xyoye.dandanplay.bean.event.TorrentStartEvent;
import com.xyoye.dandanplay.mvp.impl.DownloadManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.DownloadingTaskItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/27.
 */

public class DownloadMangerActivity extends BaseMvpActivity<DownloadManagerPresenter> implements DownloadManagerView {
    private static final int DIALOG_BIND_DANMU = 1001;

    @BindView(R.id.download_rv)
    RecyclerView downloadRv;

    private BaseRvAdapter<BtTask> adapter;
    private Runnable refresh;
    private Handler mHandler = IApplication.getMainHandler();

    @Override
    public void initView() {
        setTitle("下载管理");
        downloadRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        downloadRv.setNestedScrollingEnabled(false);
        downloadRv.setItemViewCacheSize(10);
        ((SimpleItemAnimator)downloadRv.getItemAnimator()).setSupportsChangeAnimations(false);
        presenter.getTorrentList();
        adapter = new BaseRvAdapter<BtTask>(IApplication.taskList) {
            @NonNull
            @Override
            public AdapterItem<BtTask> onCreateItem(int viewType) {
                return new DownloadingTaskItem();
            }
        };
        downloadRv.setAdapter(adapter);

        initRefresh();

        if(ServiceUtils.isServiceRunning(TorrentService.class)){
            startNewTask();
        }else {
            startTorrentService();
            presenter.observeService();
        }

        if (IApplication.isFirstOpenTaskPage){
            IApplication.isFirstOpenTaskPage = false;
            presenter.recoveryTask();
        }

    }

    private void initRefresh(){
        refresh = () -> {
            for (int i=0; i<IApplication.taskList.size(); i++){
                BtTask task = IApplication.taskList.get(i);
                if (task == null)
                    break;
                if (!task.isFinished()){
                    adapter.notifyItemChanged(i);
                }else if (!task.isRefreshAfterFinish()){
                    //任务完成后仍需刷新一次
                    task.setRefreshAfterFinish(true);
                    adapter.notifyItemChanged(i);
                }
            }
            mHandler.postDelayed(refresh,1000);
        };
        refresh.run();
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
        Torrent torrent = getIntent().getParcelableExtra("new_task");
        if (torrent != null && !IApplication.taskMap.containsKey(torrent.getHash())){
            EventBus.getDefault().post(new TorrentStartEvent(torrent));
        }
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
        showLoadingDialog("请稍等");
    }

    @Override
    public void showLoading(String msg) {
        showLoadingDialog("msg");
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
        mHandler.removeCallbacks(refresh);

        boolean isTaskRunning = false;
        for (BtTask task : IApplication.taskList) {
            if (task.isFinished()) continue;
            //没有下载任务在执行，关闭服务
            if (!task.isPaused()){
                isTaskRunning = true;
                break;
            }
        }
        if (!isTaskRunning){
            if (ServiceUtils.isServiceRunning(TorrentService.class)){
                ServiceUtils.stopService(TorrentService.class);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_download:
                new CommonDialog.Builder(DownloadMangerActivity.this)
                        .hideCancel()
                        .setAutoDismiss()
                        .build()
                        .show(getResources().getString(R.string.about_download), "关于下载", "确定", "");
                break;
            case R.id.all_start:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_START, -1));
                break;
            case R.id.all_pause:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_PAUSE, -1));
                break;
            case R.id.all_delete:
                TorrentEvent torrentEvent = new TorrentEvent();
                torrentEvent.setAction(TorrentEvent.EVENT_DELETE_ALL_TASK);
                new CommonDialog.Builder(this)
                        .showExtra()
                        .setAutoDismiss()
                        .setOkListener(dialog -> {
                            torrentEvent.setDeleteFile(false);
                            EventBus.getDefault().post(torrentEvent);
                         })
                        .setExtraListener(dialog ->{
                            torrentEvent.setDeleteFile(true);
                            EventBus.getDefault().post(torrentEvent);
                        })
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
            if (requestCode == DIALOG_BIND_DANMU){
                int episodeId = data.getIntExtra("episode_id", -1);
                String danmuPath = data.getStringExtra("path");
                int position = data.getIntExtra("position", -1);
                if (position != -1){
                    TaskBindDanmuEndEvent bindDanmuEndEvent = new TaskBindDanmuEndEvent();
                    EventBus.getDefault().post(bindDanmuEndEvent);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent event){
        if (event.getMsg() == MessageEvent.UPDATE_DOWNLOADING_TASK)
            adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TorrentBindDanmuStartEvent event){
        Intent intent = new Intent(DownloadMangerActivity.this, DanmuNetworkActivity.class);
        intent.putExtra("video_path", event.getPath());
        intent.putExtra("position", event.getPosition());
        startActivityForResult(intent, DIALOG_BIND_DANMU);
    }
}
