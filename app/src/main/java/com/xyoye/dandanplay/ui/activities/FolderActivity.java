package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
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
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.item.VideoItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderActivity extends BaseMvpActivity<FolderPresenter> implements FolderView{
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 106;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    public final static int SELECT_NETWORK_DANMU = 104;
    private int openVideoPosition = -1;

    private BaseRvAdapter<VideoBean> adapter;
    private List<VideoBean> videoList;
    private VideoBean selectVideoBean;
    private int selectPosition;
    private String folderPath;

    private boolean isLan = false;

    @Override
    public void initView() {
        videoList = new ArrayList<>();
        folderPath = getIntent().getStringExtra(OpenFolderEvent.FOLDERPATH);
        isLan = getIntent().getBooleanExtra("is_lan", false);
        String folderTitle = FileUtils.getFileNameNoExtension(folderPath.substring(0, folderPath.length()-1));
        setTitle(folderTitle);

        adapter = new BaseRvAdapter<VideoBean>(videoList) {
            @NonNull
            @Override
            public AdapterItem<VideoBean> onCreateItem(int viewType) {
                return new VideoItem();
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(adapter);

        presenter.getVideoList(folderPath);
    }

    @Override
    public void initListener() {
    }

    @Override
    public void refreshAdapter(List<VideoBean> beans) {
        videoList.clear();
        videoList.addAll(beans);
        sort(AppConfig.getInstance().getFolderSortType());
        adapter.notifyDataSetChanged();
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
    public void showLoading() {
        showLoadingDialog("正在搜索网络弹幕");
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
                int nameType = AppConfig.getInstance().getFolderSortType();
                if (nameType == Constants.Collection.NAME_ASC)
                    sort(Constants.Collection.NAME_DESC);
                else if (nameType == Constants.Collection.NAME_DESC)
                    sort(Constants.Collection.NAME_ASC);
                else
                    sort(Constants.Collection.NAME_ASC);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_by_duration:
                int durationType = AppConfig.getInstance().getFolderSortType();
                if (durationType == Constants.Collection.DURATION_ASC)
                    sort(Constants.Collection.DURATION_DESC);
                else if (durationType == Constants.Collection.DURATION_DESC)
                    sort(Constants.Collection.DURATION_ASC);
                else
                    sort(Constants.Collection.DURATION_ASC);
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
        if (ServiceUtils.isServiceRunning(SmbService.class)){
            stopService(new Intent(this, SmbService.class));
        }
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
            if (AppConfig.getInstance().isAutoLoadDanmu() && !isLan){
                if (!StringUtils.isEmpty(path)){
                    presenter.getDanmu(path);
                }
            }else {
                noMatchDanmu(path);
            }
        }else {
            openIntentVideo(videoBean);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDanmuSetting(OpenDanmuSettingEvent event){
        Intent intent = new Intent(FolderActivity.this, DanmuNetworkActivity.class);
        intent.putExtra("video_path", event.getVideoPath());
        intent.putExtra("position", event.getVideoPosition());
        intent.putExtra("is_lan", isLan);
        startActivityForResult(intent, SELECT_NETWORK_DANMU);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCurrent(SaveCurrentEvent event){
        presenter.updateCurrent(event);
        adapter.getData().get(openVideoPosition).setCurrentPosition(event.getCurrentPosition());
        adapter.notifyItemChanged(openVideoPosition);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateDanmu( OpenDanmuFolderEvent event){
        selectVideoBean.setDanmuPath(event.getPath());
        selectVideoBean.setEpisodeId(event.getEpisodeId());

        String folderPath = FileUtils.getDirName(selectVideoBean.getVideoPath());
        presenter.updateDanmu(event.getPath(), event.getEpisodeId(), new String[]{folderPath, selectVideoBean.getVideoPath()});
        adapter.notifyItemChanged(selectPosition);
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void videoAction(VideoActionEvent event){
        VideoBean videoBean = videoList.get(event.getPosition());
        switch (event.getActionType()){
            case VideoActionEvent.UN_BIND:
                videoBean.setEpisodeId(-1);
                videoBean.setDanmuPath("");
                adapter.notifyItemChanged(event.getPosition());
                String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
                presenter.updateDanmu("", -1, new String[]{folderPath, videoBean.getVideoPath()});
                break;
            case VideoActionEvent.DELETE:
                new CommonDialog.Builder(this)
                        .setAutoDismiss()
                        .setOkListener(dialog -> {
                            if(isLan){
                                presenter.deleteFile(videoBean.getVideoPath());
                                videoList.remove(event.getPosition());
                                adapter.notifyDataSetChanged();
                                return;
                            }

                            String rootPhonePath = Environment.getExternalStorageDirectory().getPath();
                            if (!videoBean.getVideoPath().startsWith(rootPhonePath)){
                                String SDFolderUri = AppConfig.getInstance().getSDFolderUri();
                                if (com.blankj.utilcode.util.StringUtils.isEmpty(SDFolderUri)) {
                                    new CommonDialog.Builder(this)
                                            .setAutoDismiss()
                                            .setOkListener(dialog1 -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                                    startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                                                } else {
                                                    ToastUtils.showShort("当前build sdk版本不支持SD卡授权");
                                                }
                                            })
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
                                                    presenter.deleteFile(videoBean.getVideoPath());
                                                    videoList.remove(event.getPosition());
                                                    adapter.notifyDataSetChanged();
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
                                                presenter.deleteFile(videoBean.getVideoPath());

                                                videoList.remove(event.getPosition());
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        })
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
                if (position < 0 || position > videoList.size()) return;

                String videoPath = videoList.get(position).getVideoPath();
                presenter.updateDanmu(danmuPath, episodeId, new String[]{folderPath, videoPath});

                videoList.get(position).setDanmuPath(danmuPath);
                videoList.get(position).setEpisodeId(episodeId);
                adapter.notifyItemChanged(position);
            }else if (requestCode == DIRECTORY_CHOOSE_REQ_CODE) {
                Uri SDCardUri = data.getData();
                if (SDCardUri != null) {
                    getContentResolver().takePersistableUriPermission(SDCardUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    AppConfig.getInstance().setSDFolderUri(SDCardUri.toString());
                } else {
                    ToastUtils.showShort("未获取外置存储卡权限，无法操作外置存储卡");
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void downloadDanmu(DanmuMatchBean.MatchesBean matchesBean){
        DanmuDownloadDialog dialog = new DanmuDownloadDialog(this, R.style.Dialog, selectVideoBean.getVideoPath(), matchesBean);
        dialog.show();
    }

    @Override
    public void noMatchDanmu(String videoPath) {
        if (!isLan){
            String danmuPath = videoPath.substring(0, videoPath.lastIndexOf("."))+ ".xml";
            File file = new File(danmuPath);
            if (file.exists()){
                selectVideoBean.setDanmuPath(danmuPath);
                ToastUtils.showShort("匹配到相同目录下同名弹幕");
            }else {
                String name = FileUtils.getFileNameNoExtension(videoPath)+ ".xml";
                danmuPath = AppConfig.getInstance().getDownloadFolder()+ "/" + name;
                file = new File(danmuPath);
                if (file.exists()){
                    selectVideoBean.setDanmuPath(danmuPath);
                    ToastUtils.showShort("匹配到下载目录下同名弹幕");
                }
            }
        }
        openIntentVideo(selectVideoBean);
    }

    @Override
    public Boolean isLan() {
        return getIntent().getBooleanExtra("is_lan", false);
    }

    @Override
    public void openIntentVideo(VideoBean videoBean){
        if (!isLan){
            if (AppConfig.getInstance().isShowMkvTips() && FileUtils.getFileExtension(videoBean.getVideoPath()).toLowerCase().equals(".MKV")){
                new CommonDialog.Builder(this)
                        .setAutoDismiss()
                        .setOkListener(dialog -> {
                            String title = FileUtils.getFileNameNoExtension(videoBean.getVideoPath());
                            Intent intent = new Intent(FolderActivity.this, PlayerActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("path", videoBean.getVideoPath());
                            intent.putExtra("danmu_path",videoBean.getDanmuPath());
                            intent.putExtra("current", videoBean.getCurrentPosition());
                            intent.putExtra("episode_id", videoBean.getEpisodeId());
                            startActivity(intent);
                        })
                        .setCancelListener(dialog -> launchActivity(PlayerSettingActivity.class))
                        .setDismissListener(dialog -> AppConfig.getInstance().hideMkvTips())
                        .build()
                        .show(getResources().getString(R.string.mkv_tips), "关于MKV格式", "我知道了", "前往设置");
            }else {
                String title = FileUtils.getFileNameNoExtension(videoBean.getVideoPath());
                Intent intent = new Intent(FolderActivity.this, PlayerActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("path", videoBean.getVideoPath());
                intent.putExtra("danmu_path",videoBean.getDanmuPath());
                intent.putExtra("current", videoBean.getCurrentPosition());
                intent.putExtra("episode_id", videoBean.getEpisodeId());
                startActivity(intent);
            }
        }else {
            if(ServiceUtils.isServiceRunning(SmbService.class)){
                String httpUrl = "http://" + LocalIPUtil.IP + ":" + LocalIPUtil.PORT + "/";
                String mSmbUrl = videoBean.getVideoPath().replace("smb://", "smb=");

                String title = FileUtils.getFileNameNoExtension(videoBean.getVideoPath());
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("path", httpUrl+mSmbUrl);
                intent.putExtra("danmu_path", videoBean.getDanmuPath());
                intent.putExtra("current", videoBean.getVideoDuration());
                intent.putExtra("episode_id", videoBean.getEpisodeId());
                startActivity(intent);
            }else {
                Intent intent = new Intent(this, SmbService.class);
                intent.putExtra("is_lan", isLan);
                intent.putExtra(OpenFolderEvent.FOLDERPATH, folderPath);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                }else {
                    startService(intent);
                }
                presenter.observeService(videoBean);
            }
        }
    }

    public void sort(int type){
        if (type == Constants.Collection.NAME_ASC){
            Collections.sort(videoList,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(FileUtils.getFileNameNoExtension(o1.getVideoPath()), FileUtils.getFileNameNoExtension(o2.getVideoPath())));
        }else if (type == Constants.Collection.NAME_DESC){
            Collections.sort(videoList,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(FileUtils.getFileNameNoExtension(o2.getVideoPath()), FileUtils.getFileNameNoExtension(o1.getVideoPath())));
        }else if (type == Constants.Collection.DURATION_ASC){
            Collections.sort(videoList,
                    (o1, o2) -> Long.compare(o1.getVideoDuration(), o2.getVideoDuration()));
        }else if (type == Constants.Collection.DURATION_DESC){
            Collections.sort(videoList,
                    (o1, o2) -> Long.compare(o2.getVideoDuration(), o1.getVideoDuration()));
        }
        AppConfig.getInstance().saveFolderSortType(type);
    }
}
