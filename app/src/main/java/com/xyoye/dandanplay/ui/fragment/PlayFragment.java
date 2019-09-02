package com.xyoye.dandanplay.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.activities.play.FolderActivity;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.FolderItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PlayFragment extends BaseMvpFragment<PlayFragmentPresenter> implements PlayFragmentView {
    public static final int UPDATE_ADAPTER_DATA = 0;
    public static final int UPDATE_DATABASE_DATA = 1;
    public static final int UPDATE_SYSTEM_DATA = 2;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(R.id.fast_play_bt)
    FloatingActionButton fastPlayBt;

    private BaseRvAdapter<FolderBean> adapter;
    private Disposable permissionDis;
    private boolean updateVideoFlag = false;

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

        FolderItem.PlayFolderListener itemListener = new FolderItem.PlayFolderListener() {
            @Override
            public void onClick(String folderPath) {
                Intent intent = new Intent(getContext(), FolderActivity.class);
                intent.putExtra(OpenFolderEvent.FOLDERPATH, folderPath);
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(String folderPath, String folderName) {
                new CommonDialog.Builder(getContext())
                        .setOkListener(dialog -> {
                            if (FileUtils.deleteDir(folderPath)){
                                refresh.setRefreshing(true);
                                refreshVideo(false);
                            }else {
                                ToastUtils.showShort("删除文件夹失败");
                            }
                        })
                        .setExtraListener(dialog -> {
                            presenter.deleteFolder(folderPath);
                            refresh.setRefreshing(true);
                            refreshVideo(false);
                        })
                        .setAutoDismiss()
                        .showExtra()
                        .build()
                        .show("确认删除文件夹["+folderName+"]？", "屏蔽目录");
                return true;
            }
        };

        adapter = new BaseRvAdapter<FolderBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<FolderBean> onCreateItem(int viewType) {
                return new FolderItem(itemListener);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);

        fastPlayBt.setOnClickListener(v -> {
            String videoPath = AppConfig.getInstance().getLastPlayVideo();
            if (!StringUtils.isEmpty(videoPath)){
                VideoBean videoBean = presenter.getLastPlayVideo(videoPath);
                if (videoBean == null)
                    return;
                //视频文件是否已被删除
                File videoFile = new File(videoBean.getVideoPath());
                if (!videoFile.exists())
                    return;
                //弹幕文件是否已被删除
                if (!StringUtils.isEmpty(videoBean.getDanmuPath())){
                    File danmuFile = new File(videoBean.getDanmuPath());
                    if (!danmuFile.exists())
                        videoBean.setDanmuPath("");
                }

                PlayerManagerActivity.launchPlayer(getContext(),
                        FileUtils.getFileNameNoExtension(videoBean.getVideoPath()),
                        videoBean.getVideoPath(),
                        videoBean.getDanmuPath(),
                        videoBean.getCurrentPosition(),
                        videoBean.getEpisodeId());
            }
        });

        if (updateVideoFlag){
            refresh.setRefreshing(true);
            initVideoData();
        }
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(() -> refreshVideo(true));
    }

    @Override
    public void refreshAdapter(List<FolderBean> beans) {
        adapter.setData(beans);
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
    public void onDestroy() {
        super.onDestroy();
        if (permissionDis != null)
            permissionDis.dispose();
    }

    public void refreshFolderData(int updateType){
        if (updateType == UPDATE_ADAPTER_DATA){
            adapter.notifyDataSetChanged();
        }else {
            refreshVideo(updateType == UPDATE_SYSTEM_DATA);
        }
    }

    public void initVideoData(){
        if (presenter == null || refresh == null){
            updateVideoFlag = true;
        } else {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            if (getContext() != null)
                getContext().sendBroadcast(intent);
            refresh.setRefreshing(true);
            presenter.refreshVideo(true);
            updateVideoFlag = false;
        }
    }

    /**
     * 刷新文件列表
     * @param reScan 是否重新扫描文件目录
     */
    @SuppressLint("CheckResult")
    private void refreshVideo(boolean reScan){
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        permissionDis = d;
                    }

                    @Override
                    public void onNext(Boolean granted) {
                        if (granted) {
                            //通知系统刷新目录
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                            if (getContext() != null)
                                getContext().sendBroadcast(intent);
                            presenter.refreshVideo(reScan);
                        }else {
                            ToastUtils.showLong("未授予文件管理权限，无法扫描视频");
                            refresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
