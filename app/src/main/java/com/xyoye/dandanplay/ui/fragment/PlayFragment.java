package com.xyoye.dandanplay.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.event.DeleteFolderEvent;
import com.xyoye.dandanplay.bean.event.ListFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.activities.FolderActivity;
import com.xyoye.dandanplay.ui.weight.dialog.DialogUtils;
import com.xyoye.dandanplay.ui.weight.item.FolderItem;
import com.xyoye.dandanplay.utils.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */

public class PlayFragment extends BaseFragment<PlayFragmentPresenter> implements PlayFragmentView {
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;
    private BaseRvAdapter<FolderBean> adapter;

    public static PlayFragment newInstance() {
        return new PlayFragment();
    }

    @NonNull
    @Override
    protected PlayFragmentPresenter initPresenter() {
        return new PlayFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_play;
    }

    @SuppressLint("CheckResult")
    @Override
    public void initView() {
        refresh.setColorSchemeResources(R.color.theme_color);

        layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);

        getVideoList();
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(this::getVideoList);
    }

    @Override
    public void refreshAdapter(List<FolderBean> beans) {
        if (adapter == null) {
            adapter = new BaseRvAdapter<FolderBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<FolderBean> onCreateItem(int viewType) {
                    return new FolderItem();
                }
            };
            if (recyclerView != null)
                recyclerView.setAdapter(adapter);
        } else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
        hideLoading();
        if (refresh != null)
            refresh.setRefreshing(false);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @Override
    protected void onPageFirstVisible() {
        super.onPageFirstVisible();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
        }else {
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openFolder(OpenFolderEvent event) {
        Intent intent = new Intent(getContext(), FolderActivity.class);
        intent.putExtra(OpenFolderEvent.FOLDERPATH, event.getFolderPath());
        intent.putExtra(OpenFolderEvent.FOLDERTITLE, event.getFolderTitle());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deleteEvent(DeleteFolderEvent event){
        new DialogUtils.Builder(getContext())
                .setOkListener(dialog ->{
                    dialog.dismiss();
                    if (!event.getFolderPath().startsWith(com.xyoye.dandanplay.utils.FileUtils.Base_Path)){
                        ToastUtils.showShort("很抱歉，目前暂不支持管理外置储存卡文件");
                    }else {
                        new RxPermissions(this).
                                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribe(granted -> {
                                    if (granted) {
                                        File file = new File(event.getFolderPath());
                                        if (file.exists())
                                            FileUtils.deleteFile(file);
                                        presenter.deleteFolder(event.getFolderPath());
                                    }
                                });
                    }
                })
                .setCancelListener(DialogUtils::dismiss)
                .build()
                .show("确认删除文件和记录？", true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void listFolderEvent(ListFolderEvent event){
        presenter.listFolder(event.getPath());
    }

    @SuppressLint("CheckResult")
    private void getVideoList(){
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        presenter.getVideoList();
                    }
                });
    }
}
