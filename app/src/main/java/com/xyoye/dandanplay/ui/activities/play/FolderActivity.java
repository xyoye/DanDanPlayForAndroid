package com.xyoye.dandanplay.ui.activities.play;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.BindDanmuBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.bean.params.BindDanmuParam;
import com.xyoye.dandanplay.mvp.impl.FolderPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.activities.setting.PlayerSettingActivity;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.item.VideoItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

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
 * Created by xyoye on 2018/6/30.
 */


public class FolderActivity extends BaseMvpActivity<FolderPresenter> implements FolderView {
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    public final static int SELECT_NETWORK_DANMU = 104;

    private BaseRvAdapter<VideoBean> adapter;
    private List<VideoBean> videoList;
    private VideoBean selectVideoBean;
    private int selectPosition;
    private String folderPath;

    @Override
    public void initView() {
        videoList = new ArrayList<>();
        folderPath = getIntent().getStringExtra(OpenFolderEvent.FOLDERPATH);
        String folderTitle = FileUtils.getFileNameNoExtension(folderPath.substring(0, folderPath.length() - 1));
        setTitle(folderTitle);

        adapter = new BaseRvAdapter<VideoBean>(videoList) {
            @NonNull
            @Override
            public AdapterItem<VideoBean> onCreateItem(int viewType) {
                return new VideoItem(new VideoItem.VideoItemEventListener() {
                    @Override
                    public void openDanmuSetting(int position) {
                        selectVideoBean = videoList.get(position);
                        String videoPath = videoList.get(position).getVideoPath();
                        BindDanmuParam param = new BindDanmuParam(videoPath, position);
                        Intent intent = new Intent(FolderActivity.this, DanmuNetworkActivity.class);
                        intent.putExtra("bind_param", param);
                        startActivityForResult(intent, SELECT_NETWORK_DANMU);
                    }

                    @Override
                    public void openVideo(int position) {
                        selectVideoBean = videoList.get(position);
                        selectPosition = position;
                        //未设置弹幕情况下，1、开启自动加载时自动加载，2、自动匹配相同目录下同名弹幕，3、匹配默认下载目录下同名弹幕
                        if (StringUtils.isEmpty(selectVideoBean.getDanmuPath())) {
                            String path = selectVideoBean.getVideoPath();
                            if (AppConfig.getInstance().isAutoLoadDanmu()) {
                                if (!StringUtils.isEmpty(path)) {
                                    presenter.getDanmu(path);
                                }
                            } else {
                                noMatchDanmu(path);
                            }
                        } else {
                            openIntentVideo(selectVideoBean);
                        }
                    }

                    @Override
                    public void unBindDanmu(int position) {
                        VideoBean videoBean = videoList.get(position);
                        videoBean.setEpisodeId(-1);
                        videoBean.setDanmuPath("");
                        adapter.notifyItemChanged(position);
                        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
                        presenter.updateDanmu("", -1, new String[]{folderPath, videoBean.getVideoPath()});
                    }

                    @Override
                    public void onDelete(int position) {
                        new CommonDialog.Builder(FolderActivity.this)
                                .setOkListener(dialog -> {
                                    String path = videoList.get(position).getVideoPath();
                                    if (FileUtils.delete(path)) {
                                        adapter.removeItem(position);
                                        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_DATABASE_DATA));
                                    } else {
                                        ToastUtils.showShort("删除文件失败");
                                    }
                                })
                                .setAutoDismiss()
                                .build()
                                .show("确认删除文件？");
                    }
                });
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
        switch (item.getItemId()) {
            case R.id.sort_by_name:
                int nameType = AppConfig.getInstance().getFolderSortType();
                if (nameType == Constants.FolderSort.NAME_ASC)
                    sort(Constants.FolderSort.NAME_DESC);
                else if (nameType == Constants.FolderSort.NAME_DESC)
                    sort(Constants.FolderSort.NAME_ASC);
                else
                    sort(Constants.FolderSort.NAME_ASC);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_by_duration:
                int durationType = AppConfig.getInstance().getFolderSortType();
                if (durationType == Constants.FolderSort.DURATION_ASC)
                    sort(Constants.FolderSort.DURATION_DESC);
                else if (durationType == Constants.FolderSort.DURATION_DESC)
                    sort(Constants.FolderSort.DURATION_ASC);
                else
                    sort(Constants.FolderSort.DURATION_ASC);
                adapter.notifyDataSetChanged();
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (ServiceUtils.isServiceRunning(SmbService.class)) {
            stopService(new Intent(this, SmbService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCurrent(SaveCurrentEvent event) {
        adapter.getData().get(selectPosition).setCurrentPosition(event.getCurrentPosition());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_NETWORK_DANMU) {
                BindDanmuBean bindDanmuBean = getIntent().getParcelableExtra("bind_data");
                if (bindDanmuBean == null)
                    return;

                String danmuPath = bindDanmuBean.getDanmuPath();
                int episodeId = bindDanmuBean.getEpisodeId();
                int position = bindDanmuBean.getItemPosition();

                if (position < 0 || position > videoList.size() || videoList.size() == 0)
                    return;

                String videoPath = videoList.get(position).getVideoPath();
                presenter.updateDanmu(danmuPath, episodeId, new String[]{folderPath, videoPath});

                videoList.get(position).setDanmuPath(danmuPath);
                videoList.get(position).setEpisodeId(episodeId);
                adapter.notifyItemChanged(position);
            }
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void downloadDanmu(DanmuMatchBean.MatchesBean matchesBean) {
        new DanmuDownloadDialog(this, selectVideoBean.getVideoPath(), matchesBean, (danmuPath, episodeId) -> {
            selectVideoBean.setDanmuPath(danmuPath);
            selectVideoBean.setEpisodeId(episodeId);

            String folderPath = FileUtils.getDirName(selectVideoBean.getVideoPath());
            presenter.updateDanmu(danmuPath, episodeId, new String[]{folderPath, selectVideoBean.getVideoPath()});
            adapter.notifyItemChanged(selectPosition);

            openIntentVideo(selectVideoBean);
        }).show();
    }

    @Override
    public void noMatchDanmu(String videoPath) {
        String danmuPath = videoPath.substring(0, videoPath.lastIndexOf(".")) + ".xml";
        File file = new File(danmuPath);
        if (file.exists()) {
            selectVideoBean.setDanmuPath(danmuPath);
            ToastUtils.showShort("匹配到相同目录下同名弹幕");
        } else {
            String name = FileUtils.getFileNameNoExtension(videoPath) + ".xml";
            danmuPath = AppConfig.getInstance().getDownloadFolder() + "/" + name;
            file = new File(danmuPath);
            if (file.exists()) {
                selectVideoBean.setDanmuPath(danmuPath);
                ToastUtils.showShort("匹配到下载目录下同名弹幕");
            }
        }
        openIntentVideo(selectVideoBean);
    }

    @Override
    public void openIntentVideo(VideoBean videoBean) {
        boolean isExoPlayer = AppConfig.getInstance().getPlayerType() == com.xyoye.player.commom.utils.Constants.EXO_PLAYER;
        if (!isExoPlayer && FileUtils.getFileExtension(videoBean.getVideoPath()).toLowerCase().equals(".MKV") && AppConfig.getInstance().isShowMkvTips()) {
            new CommonDialog.Builder(this)
                    .setAutoDismiss()
                    .setOkListener(dialog -> launchPlay(videoBean))
                    .setCancelListener(dialog -> launchActivity(PlayerSettingActivity.class))
                    .setDismissListener(dialog -> AppConfig.getInstance().hideMkvTips())
                    .build()
                    .show(getResources().getString(R.string.mkv_tips), "关于MKV格式", "我知道了", "前往设置");
        } else {
            launchPlay(videoBean);
        }
    }

    /**
     * 启动播放器
     *
     * @param videoBean 数据
     */
    private void launchPlay(VideoBean videoBean) {
        //记录此次播放
        AppConfig.getInstance().setLastPlayVideo(videoBean.getVideoPath());
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_ADAPTER_DATA));

        PlayerManagerActivity.launchPlayerLocal(
                this,
                FileUtils.getFileNameNoExtension(videoBean.getVideoPath()),
                videoBean.getVideoPath(),
                videoBean.getDanmuPath(),
                videoBean.getCurrentPosition(),
                videoBean.getEpisodeId());
    }

    public void sort(int type) {
        if (type == Constants.FolderSort.NAME_ASC) {
            Collections.sort(videoList,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(FileUtils.getFileNameNoExtension(o1.getVideoPath()), FileUtils.getFileNameNoExtension(o2.getVideoPath())));
        } else if (type == Constants.FolderSort.NAME_DESC) {
            Collections.sort(videoList,
                    (o1, o2) -> Collator.getInstance(Locale.CHINESE).compare(FileUtils.getFileNameNoExtension(o2.getVideoPath()), FileUtils.getFileNameNoExtension(o1.getVideoPath())));
        } else if (type == Constants.FolderSort.DURATION_ASC) {
            Collections.sort(videoList,
                    (o1, o2) -> Long.compare(o1.getVideoDuration(), o2.getVideoDuration()));
        } else if (type == Constants.FolderSort.DURATION_DESC) {
            Collections.sort(videoList,
                    (o1, o2) -> Long.compare(o2.getVideoDuration(), o1.getVideoDuration()));
        }
        AppConfig.getInstance().saveFolderSortType(type);
    }
}
