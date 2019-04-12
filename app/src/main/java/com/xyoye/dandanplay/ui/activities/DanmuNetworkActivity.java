package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.event.DownloadDanmuEvent;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.bean.event.SearchDanmuEvent;
import com.xyoye.dandanplay.mvp.impl.DanmuNetworkPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SearchDanmuDialog;
import com.xyoye.dandanplay.ui.weight.item.DanmuNetworkItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuNetworkActivity extends BaseMvpActivity<DanmuNetworkPresenter> implements DanmuNetworkView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private String videoPath;
    private BaseRvAdapter<DanmuMatchBean.MatchesBean> adapter;


    @Override
    public void initView() {
        setTitle("选择网络弹幕");
        adapter = new BaseRvAdapter<DanmuMatchBean.MatchesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<DanmuMatchBean.MatchesBean> onCreateItem(int viewType) {
                return new DanmuNetworkItem();
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new ItemDecorationSpaces(0, 0, 0, 1));
        recyclerView.setAdapter(adapter);
        videoPath = getIntent().getStringExtra("video_path");
    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local_danmu:
                new FileManagerDialog(this, FileManagerDialog.SELECT_DANMU, path ->{
                    Intent intent = getIntent();
                    intent.putExtra("path", path);
                    intent.putExtra("position", getIntent().getIntExtra("position", -1));
                    setResult(RESULT_OK, intent);
                    DanmuNetworkActivity.this.finish();
                }).show();
                break;
            case R.id.search_danmu:
                SearchDanmuDialog danmuDialog = new SearchDanmuDialog(DanmuNetworkActivity.this, R.style.Dialog);
                danmuDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_danmu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    @Override
    protected DanmuNetworkPresenter initPresenter() {
        return new DanmuNetworkPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_danmu_network;
    }

    @Override
    public String getVideoPath() {
        return videoPath;
    }

    @Override
    public void refreshAdapter(List<DanmuMatchBean.MatchesBean> beans) {
        adapter.setData(beans);
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void downloadDanmu(DownloadDanmuEvent event) {
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        DanmuMatchBean.MatchesBean bean = event.getModel();
                        DanmuDownloadDialog dialog = new DanmuDownloadDialog(this, R.style.Dialog, videoPath, bean);
                        dialog.show();
                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void searchDanmu(SearchDanmuEvent event) {
        presenter.searchDanmu(event.getAnime(), event.getEpisode());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setDanmu(OpenDanmuFolderEvent event) {
        String path = event.getPath();
        Intent intent = getIntent();
        intent.putExtra("episode_id", event.getEpisodeId());
        intent.putExtra("path", path);
        intent.putExtra("position", getIntent().getIntExtra("position", -1));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }
}
