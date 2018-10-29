package com.xyoye.dandanplay.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.event.DeleteFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.activities.FileManagerActivity;
import com.xyoye.dandanplay.ui.activities.FolderActivity;
import com.xyoye.dandanplay.ui.weight.dialog.DialogUtils;
import com.xyoye.dandanplay.utils.FileUtils;
import com.xyoye.dandanplay.utils.permission.PermissionHelper;
import com.xyoye.dandanplay.ui.weight.item.FolderItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by YE on 2018/6/29 0029.
 */

public class PlayFragment extends BaseFragment<PlayFragmentPresenter> implements PlayFragmentView {
    public final static int SELECT_FOLDER = 103;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
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

    @Override
    public void initView() {
        setHasOptionsMenu(true);
        getBaseActivity().setSupportActionBar(toolbar);
        showLoading();
        new PermissionHelper().with(this).request(() ->
                presenter.getVideoList(),
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        refresh.setColorSchemeResources(R.color.theme_color);

        layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(() ->
                new PermissionHelper()
                        .with(PlayFragment.this)
                        .request(() ->
                                presenter.getVideoList(),
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE));
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
                .setOkListener(dialog ->
                        new PermissionHelper().request(() -> {
                            dialog.dismiss();
                            showLoading();
                            File file = new File(event.getFolderPath());
                            if (file.exists())
                                FileUtils.deleteFile(file);

                            presenter.deleteFolder(event.getFolderPath());
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
                .setCancelListener(DialogUtils::dismiss)
                .build()
                .show("确认删除文件和记录？", true, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_video_folder:
                addFolder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Activity activity = getActivity();
        if (activity != null)
            activity.getMenuInflater().inflate(R.menu.menu_add, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    private void addFolder() {
        Intent intent = new Intent(getContext(), FileManagerActivity.class);
        intent.putExtra("file_type", FileManagerActivity.FILE_FOLDER);
        startActivityForResult(intent, SELECT_FOLDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FOLDER) {
                String folderPath = data.getStringExtra("folder");
                presenter.listFolder(folderPath);
            }
        }
    }
}
