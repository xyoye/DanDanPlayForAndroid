package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.event.DeleteFolderEvent;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.LanFolderPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LanFolderPresenter;
import com.xyoye.dandanplay.mvp.view.LanFolderView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.FolderItem;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyy on 2018/11/21.
 */

public class LanFolderActivity extends BaseMvpActivity<LanFolderPresenter> implements LanFolderView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.folder_rv)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private LinearLayoutManager mLayoutManager;
    private BaseRvAdapter<FolderBean> mAdapter;
    private List<FolderBean> mFolderBeanList;

    @NonNull
    @Override
    protected LanFolderPresenter initPresenter() {
        return new LanFolderPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.acitivity_lan_folder;
    }

    @Override
    public void initView() {
        setTitle("局域网");
        EventBus.getDefault().register(this);
        refresh.setColorSchemeResources(R.color.theme_color);

        mFolderBeanList = new ArrayList<>();
        mAdapter = new BaseRvAdapter<FolderBean>(mFolderBeanList) {
            @NonNull
            @Override
            public AdapterItem<FolderBean> onCreateItem(int viewType) {
                return new FolderItem();
            }
        };

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(mAdapter);

        String device = SPUtils.getInstance().getString(Constants.Config.SMB_DEVICE);
        if (StringUtils.isEmpty(device)){
            launchActivity(LanDeviceDeviceActivity.class);
        }else {
            presenter.getFolders();
        }
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(() -> presenter.searchFolder());
    }

    @Override
    public void refreshFolder(List<FolderBean> folderBeans) {
        if (refresh != null && refresh.isRefreshing())
            refresh.setRefreshing(false);
        mFolderBeanList.clear();
        mFolderBeanList.addAll(folderBeans);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void updateFolder(MessageEvent event){
        if (event.getMsg() == MessageEvent.UPDATE_LAN_FOLDER){
            presenter.getFolders();
            refresh.setRefreshing(true);
        }
    }

    @Subscribe
    public void deleteEvent(DeleteFolderEvent event){
        new CommonDialog.Builder(this)
                .setAutoDismiss()
                .setOkListener(dialog -> {
                    mFolderBeanList.remove(event.getPosition());
                    mAdapter.notifyDataSetChanged();
                    presenter.deleteFolder(event.getFolderPath());
                })
                .build()
                .show("确认删除此文件夹？");
    }

    @Subscribe
    public void openEvent(OpenFolderEvent event){
        Intent intent = new Intent(this, FolderActivity.class);
        intent.putExtra(OpenFolderEvent.FOLDERPATH, event.getFolderPath());
        intent.putExtra("is_lan", true);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_update_device:
                launchActivity(LanDeviceDeviceActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lan_folder, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
