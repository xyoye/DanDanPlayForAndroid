package com.xyoye.dandanplay.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.event.DownloadDanmuEvent;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.bean.event.SearchDanmuEvent;
import com.xyoye.dandanplay.mvp.impl.DanmuNetworkPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;
import com.xyoye.dandanplay.ui.weight.SpacesItemDecoration;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SearchDanmuDialog;
import com.xyoye.dandanplay.ui.weight.item.DanmuNetworkItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuNetworkActivity extends BaseActivity<DanmuNetworkPresenter> implements DanmuNetworkView {
    public final static int SELECT_DANMU = 101;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<DanmuMatchBean.MatchesBean> adapter;


    @Override
    public void initView() {
        setTitle("选择网络弹幕");
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, 1));
    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local_danmu:
                Intent intent = new Intent(this, FileManagerActivity.class);
                intent.putExtra("file_type", FileManagerActivity.FILE_DANMU);
                startActivityForResult(intent, SELECT_DANMU);
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
        return getIntent().getStringExtra("path");
    }

    @Override
    public void refreshAdapter(List<DanmuMatchBean.MatchesBean> beans) {
        if (adapter == null) {
            adapter = new BaseRvAdapter<DanmuMatchBean.MatchesBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<DanmuMatchBean.MatchesBean> onCreateItem(int viewType) {
                    return new DanmuNetworkItem();
                }
            };
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void downloadDanmu(DownloadDanmuEvent event) {
        DanmuMatchBean.MatchesBean bean = event.getModel();
        DanmuDownloadDialog dialog = new DanmuDownloadDialog(this, R.style.Dialog, bean);
        dialog.show();
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
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_DANMU) {
                Intent intent = getIntent();
                intent.putExtra("path", data.getStringExtra("danmu"));
                intent.putExtra("position", getIntent().getIntExtra("position", -1));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
