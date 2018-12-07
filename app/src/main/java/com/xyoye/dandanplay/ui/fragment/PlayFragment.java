package com.xyoye.dandanplay.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.event.DeleteFolderEvent;
import com.xyoye.dandanplay.bean.event.ListFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.activities.FolderActivity;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.FolderItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

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
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 106;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openFolder(OpenFolderEvent event) {
        Intent intent = new Intent(getContext(), FolderActivity.class);
        intent.putExtra(OpenFolderEvent.FOLDERPATH, event.getFolderPath());
        startActivity(intent);
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deleteEvent(DeleteFolderEvent event){
        new CommonDialog.Builder(getContext())
                .setAutoDismiss()
                .setOkListener(dialog -> {
                    String rootPhonePath = Environment.getExternalStorageDirectory().getPath();
                    if (!event.getFolderPath().startsWith(rootPhonePath)){
                        String SDFolderUri = AppConfig.getInstance().getSDFolderUri();
                        if (StringUtils.isEmpty(SDFolderUri)) {
                            new CommonDialog.Builder(getContext())
                                    .setAutoDismiss()
                                    .setOkListener(dialog1 -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                            startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                                        } else {
                                            ToastUtils.showShort("外置存储文件操作需要手动授权，确认跳转后，请选择外置存储卡");
                                        }
                                    })
                                    .build()
                                    .show("确认删除此文件夹？");
                        }else {
                            DocumentFile documentFile = DocumentFile.fromTreeUri(getContext(), Uri.parse(SDFolderUri));
                            List<String> rootPaths = SDCardUtils.getSDCardPaths();
                            for (String rootPath : rootPaths){
                                if (event.getFolderPath().startsWith(rootPath)){
                                    String folder = event.getFolderPath().replace(rootPath, "");
                                    String[] folders = folder.split("/");
                                    for (int i = 0; i < folders.length; i++) {
                                        String aFolder = folders[i];
                                        if(StringUtils.isEmpty(aFolder))continue;
                                        documentFile = documentFile.findFile(aFolder);
                                        if (documentFile == null || !documentFile.exists()){
                                            ToastUtils.showShort("找不到该文件夹");
                                            return;
                                        }
                                        if (i == folders.length-1){
                                            documentFile.delete();
                                            presenter.deleteFolder(event.getFolderPath());
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        new RxPermissions(this).
                                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribe(granted -> {
                                    if (granted) {
                                        File folder = new File(event.getFolderPath());
                                        if (folder.exists())
                                            com.blankj.utilcode.util.FileUtils.deleteDir(folder);
                                        presenter.deleteFolder(event.getFolderPath());
                                    }
                                });
                    }
                })
                .build()
                .show("确认删除文件和记录？");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == DIRECTORY_CHOOSE_REQ_CODE){
                Uri SDCardUri = data.getData();
                if (SDCardUri != null){
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.getContentResolver().takePersistableUriPermission(SDCardUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        AppConfig.getInstance().setSDFolderUri(SDCardUri.toString());
                    }
                }else {
                    ToastUtils.showShort("未获取外置存储卡权限，无法操作外置存储卡");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void registerEventBus(){
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    public void unrigisterEventBus(){
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
