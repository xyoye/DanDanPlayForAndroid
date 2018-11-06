package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenVideoEvent;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.event.VideoActionEvent;
import com.xyoye.dandanplay.mvp.impl.FolderPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.dialog.DialogUtils;
import com.xyoye.dandanplay.ui.weight.item.VideoItem;
import com.xyoye.dandanplay.utils.AppConfigShare;
import com.xyoye.dandanplay.utils.Config;
import com.xyoye.dandanplay.utils.UserInfoShare;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderActivity extends BaseActivity<FolderPresenter> implements FolderView{
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 106;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    public final static int SELECT_NETWORK_DANMU = 104;
    private int openVideoPosition = -1;

    private BaseRvAdapter<VideoBean> adapter;
    private List<VideoBean> videoBeans;
    private VideoBean selectVideoBean;
    private int selectPosition;

    @Override
    public void initView() {
        String title = getIntent().getStringExtra(OpenFolderEvent.FOLDERTITLE);
        setTitle(title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);

        showLoading();
        presenter.refreshVideos();
    }

    @Override
    public void initListener() {
    }

    @Override
    public void refreshAdapter(List<VideoBean> beans) {
        videoBeans = beans;
        sort(UserInfoShare.getInstance().getFolderCollectionsType());
        if (adapter == null){
            adapter = new BaseRvAdapter<VideoBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<VideoBean> onCreateItem(int viewType) {
                    return new VideoItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
        hideLoading();
    }

    @NonNull
    @Override
    protected FolderPresenter initPresenter() {
        return new FolderPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_folder;
    }

    @Override
    public String getFolderPath() {
        return getIntent().getStringExtra(OpenFolderEvent.FOLDERPATH);
    }

    @Override
    public void showLoading() {
        showLoadingDialog("正在搜索网络弹幕", false);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_by_name:
                int nameType = UserInfoShare.getInstance().getFolderCollectionsType();
                if (nameType == Config.Collection.NAME_ASC)
                    sort(Config.Collection.NAME_DESC);
                else if (nameType == Config.Collection.NAME_DESC)
                    sort(Config.Collection.NAME_ASC);
                else
                    sort(Config.Collection.NAME_ASC);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_by_duration:
                int durationType = UserInfoShare.getInstance().getFolderCollectionsType();
                if (durationType == Config.Collection.DURATION_ASC)
                    sort(Config.Collection.DURATION_DESC);
                else if (durationType == Config.Collection.DURATION_DESC)
                    sort(Config.Collection.DURATION_ASC);
                else
                    sort(Config.Collection.DURATION_ASC);
                adapter.notifyDataSetChanged();
                break;
            case R.id.player_setting:
                launchActivity(PlayerSettingActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return super.onCreateOptionsMenu(menu);
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
    public void openVideo(OpenVideoEvent event){
        openVideoPosition = event.getPosition();
        VideoBean videoBean = event.getBean();
        selectVideoBean = videoBean;
        selectPosition = event.getPosition();
        //未设置弹幕情况下，1、开启自动加载时自动加载，2、自动匹配相同目录下同名弹幕，3、匹配默认下载目录下同名弹幕
        if (StringUtils.isEmpty(videoBean.getDanmuPath())){
            String path = videoBean.getVideoPath();
            if (AppConfigShare.getInstance().isAutoLoadDanmu()){
                if (!StringUtils.isEmpty(path)){
                    presenter.getDanmu(path);
                }
            }else {
                noMatchDanmu(path);
            }
        }else {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("title", videoBean.getVideoName());
            intent.putExtra("path",videoBean.getVideoPath());
            intent.putExtra("danmu_path",videoBean.getDanmuPath());
            intent.putExtra("current", videoBean.getCurrentPosition());
            intent.putExtra("episode_id", videoBean.getEpisodeId());
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDanmuSetting(OpenDanmuSettingEvent event){
        Intent intent = new Intent(FolderActivity.this, DanmuNetworkActivity.class);
        intent.putExtra("path", event.getVideoPath());
        intent.putExtra("position", event.getVideoPosition());
        startActivityForResult(intent, SELECT_NETWORK_DANMU);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCurrent(SaveCurrentEvent event){
        presenter.updateCurrent(event);
        adapter.getData().get(openVideoPosition).setCurrentPosition(event.getCurrentPosition());
        adapter.notifyItemChanged(openVideoPosition);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openVideo( OpenDanmuFolderEvent event){
        selectVideoBean.setDanmuPath(event.getPath());
        selectVideoBean.setEpisodeId(event.getEpisodeId());

        String folderPath = FileUtils.getDirName(selectVideoBean.getVideoPath());
        String fileName = FileUtils.getFileName(selectVideoBean.getVideoPath());
        presenter.updateDanmu(event.getPath(), event.getEpisodeId(), new String[]{folderPath, fileName});
        adapter.notifyItemChanged(selectPosition);

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", selectVideoBean.getVideoName());
        intent.putExtra("path", selectVideoBean.getVideoPath());
        intent.putExtra("danmu_path",selectVideoBean.getDanmuPath());
        intent.putExtra("current", selectVideoBean.getCurrentPosition());
        intent.putExtra("episode_id", selectVideoBean.getEpisodeId());
        startActivity(intent);
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void videoAction(VideoActionEvent event){
        VideoBean videoBean = videoBeans.get(event.getPosition());
        switch (event.getActionType()){
            case VideoActionEvent.UN_BIND:
                videoBean.setEpisodeId(-1);
                videoBean.setDanmuPath("");
                adapter.notifyItemChanged(event.getPosition());
                String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
                String fileName = FileUtils.getFileName(videoBean.getVideoPath());
                presenter.updateDanmu("", -1, new String[]{folderPath, fileName});
                break;
            case VideoActionEvent.DELETE:
                new DialogUtils.Builder(this)
                        .setOkListener(dialog -> {
                            dialog.dismiss();
                            if (!videoBean.getVideoPath().startsWith(com.xyoye.dandanplay.utils.FileUtils.Base_Path)){
                                String SDFolderUri = AppConfigShare.getInstance().getSDFolderUri();
                                if (com.blankj.utilcode.util.StringUtils.isEmpty(SDFolderUri)) {
                                    new DialogUtils.Builder(FolderActivity.this)
                                            .setOkListener(dialog1 -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                    startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                                                } else {
                                                    ToastUtils.showShort("当前build sdk版本不支持SD卡授权");
                                                }
                                            })
                                            .setCancelListener(DialogUtils::dismiss)
                                            .build()
                                            .show("外置存储文件操作需要手动授权，确认跳转后，请选择外置存储卡");
                                }else {
                                    DocumentFile documentFile = DocumentFile.fromTreeUri(this, Uri.parse(SDFolderUri));
                                    List<String> rootPaths = SDCardUtils.getSDCardPaths();
                                    for (String rootPath : rootPaths){
                                        if (videoBean.getVideoPath().startsWith(rootPath)){
                                            String folder = videoBean.getVideoPath().replace(rootPath, "");
                                            String[] folders = folder.split("/");
                                            for (int i = 0; i < folders.length; i++) {
                                                String aFolder = folders[i];
                                                if(com.blankj.utilcode.util.StringUtils.isEmpty(aFolder))continue;
                                                documentFile = documentFile.findFile(aFolder);
                                                if (documentFile == null || !documentFile.exists()){
                                                    ToastUtils.showShort("找不到该文件");
                                                    return;
                                                }
                                                if (i == folders.length-1){
                                                    documentFile.delete();

                                                    String deleteFolderPath = FileUtils.getDirName(videoBean.getVideoPath());
                                                    String deleteFileName = FileUtils.getFileName(videoBean.getVideoPath());
                                                    presenter.deleteFile(deleteFolderPath, deleteFileName);
                                                    videoBeans.remove(event.getPosition());
                                                    adapter.notifyItemChanged(event.getPosition());
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
                                                File file = new File(videoBean.getVideoPath());
                                                if (file.exists())
                                                    file.delete();

                                                String deleteFolderPath = FileUtils.getDirName(videoBean.getVideoPath());
                                                String deleteFileName = FileUtils.getFileName(videoBean.getVideoPath());
                                                presenter.deleteFile(deleteFolderPath, deleteFileName);

                                                videoBeans.remove(event.getPosition());
                                                adapter.notifyItemChanged(event.getPosition());
                                            }
                                        });
                            }
                        })
                        .setCancelListener(DialogUtils::dismiss)
                        .build()
                        .show("确认删除该文件？");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_NETWORK_DANMU){
                String danmuPath = data.getStringExtra("path");
                int episodeId = data.getIntExtra("episode_id", 0);
                int position = data.getIntExtra("position", -1);
                if (position < 0) return;
                String videoPath = adapter.getData().get(position).getVideoPath();
                String folderPath = FileUtils.getDirName(videoPath);
                String fileName = FileUtils.getFileName(videoPath);
                presenter.updateDanmu(danmuPath, episodeId, new String[]{folderPath, fileName});
                adapter.getData().get(position).setDanmuPath(danmuPath);
                adapter.getData().get(position).setEpisodeId(episodeId);
                adapter.notifyItemChanged(position);
            }else if (requestCode == DIRECTORY_CHOOSE_REQ_CODE) {
                Uri SDCardUri = data.getData();
                if (SDCardUri != null) {
                    getContentResolver().takePersistableUriPermission(SDCardUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    AppConfigShare.getInstance().setSDFolderUri(SDCardUri.toString());
                } else {
                    ToastUtils.showShort("未获取外置存储卡权限，无法操作外置存储卡");
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void downloadDanmu(DanmuMatchBean.MatchesBean matchesBean){
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        DanmuDownloadDialog dialog = new DanmuDownloadDialog(this, R.style.Dialog, matchesBean);
                        dialog.show();
                    }
                });
    }

    @Override
    public void noMatchDanmu(String videoPath) {
        String danmuPath = videoPath.substring(0, videoPath.lastIndexOf("."))+ ".xml";
        File file = new File(danmuPath);
        if (file.exists()){
            selectVideoBean.setDanmuPath(danmuPath);
            ToastUtils.showShort("匹配到相同目录下同名弹幕");
        }else {
            String ext = FileUtils.getFileExtension(videoPath);
            String name = FileUtils.getFileName(videoPath).replace(ext, "xml");
            danmuPath = AppConfigShare.getInstance().getDownloadFolder()+ "/" + name;
            file = new File(danmuPath);
            if (file.exists()){
                selectVideoBean.setDanmuPath(danmuPath);
                ToastUtils.showShort("匹配到下载目录下同名弹幕");
            }
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", selectVideoBean.getVideoName());
        intent.putExtra("path",selectVideoBean.getVideoPath());
        intent.putExtra("danmu_path",selectVideoBean.getDanmuPath());
        intent.putExtra("current", selectVideoBean.getCurrentPosition());
        intent.putExtra("episode_id", selectVideoBean.getEpisodeId());
        startActivity(intent);
    }

    public void sort(int type){
        if (type == Config.Collection.NAME_ASC){
            Collections.sort(videoBeans,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(o1.getVideoName(), o2.getVideoName()));
        }else if (type == Config.Collection.NAME_DESC){
            Collections.sort(videoBeans,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(o2.getVideoName(), o1.getVideoName()));
        }else if (type == Config.Collection.DURATION_ASC){
            Collections.sort(videoBeans,
                    (o1, o2) -> o1.getVideoDuration() > o2.getVideoDuration() ? 1 : -1);
        }else if (type == Config.Collection.DURATION_DESC){
            Collections.sort(videoBeans,
                    (o1, o2) -> o1.getVideoDuration() < o2.getVideoDuration() ? 1 : -1);
        }
        UserInfoShare.getInstance().saveFolderCollectionsType(type);
    }
}
