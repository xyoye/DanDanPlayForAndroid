package com.xyoye.dandanplay.ui.folderMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.PixelUtil;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.event.OpenFolderEvent;
import com.xyoye.dandanplay.event.OpenVideoEvent;
import com.xyoye.dandanplay.event.SaveCurrentEvent;
import com.xyoye.dandanplay.mvp.impl.FolderPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.ui.fileManagerMod.FileManagerActivity;
import com.xyoye.dandanplay.ui.playMod.PlayerActivity;
import com.xyoye.dandanplay.weight.decorator.SpacesItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderActivity extends BaseActivity<FolderPresenter> implements FolderView{
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    public final static int SELECT_DANMU = 101;
    public final static int OPEN_VIDEO = 102;
    private int selectItem = -1;
    private int openVideoPosition = -1;

    private BaseRvAdapter<VideoBean> adapter;

    @Override
    public void initView() {
        String title = getIntent().getStringExtra(OpenFolderEvent.FOLDERTITLE);
        setTitle(title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
//        recyclerView.addItemDecoration(new DividerItemDecoration(
//                this, DividerItemDecoration.VERTICAL));

        showLoading();
        presenter.refreshVideos();
    }

    @Override
    public void initListener() {
    }

    @Override
    public void refreshAdapter(List<VideoBean> beans) {
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

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
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
        //未设置弹幕情况下，自动匹配同名弹幕
        if (StringUtils.isEmpty(videoBean.getDanmuPath())){
            String path = videoBean.getVideoPath();
            int dot = path.lastIndexOf(".");
            path = path.substring(0, dot);
            String danmuPath = path + ".xml";
            File file = new File(danmuPath);
            if (file.exists())
                videoBean.setDanmuPath(danmuPath);
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("title", videoBean.getVideoName());
        intent.putExtra("path",videoBean.getVideoPath());
        intent.putExtra("danmu_path",videoBean.getDanmuPath());
        intent.putExtra("current", videoBean.getCurrentPosition());
        startActivityForResult(intent, OPEN_VIDEO);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDanmuSetting(OpenDanmuSettingEvent event){
        selectItem = event.getVideoPosition();
        Intent intent = new Intent(this, FileManagerActivity.class);
        intent.putExtra(FileManagerActivity.IS_FOLDER, false);
        intent.putExtra(FileManagerActivity.VIDEO_PATH, event.getVideoPath());
        startActivityForResult(intent, SELECT_DANMU);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCurrent(SaveCurrentEvent event){
        presenter.updateCurrent(event);
        adapter.getData().get(openVideoPosition).setCurrentPosition(event.getCurrentPosition());
        adapter.notifyItemChanged(openVideoPosition);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_DANMU){
                String danmuPath = data.getStringExtra("danmu");
                String videoPath = adapter.getData().get(selectItem).getVideoPath();
                String folderPath = FileUtils.getDirName(videoPath);
                String fileName = FileUtils.getFileName(videoPath);
                presenter.updateDanmu(danmuPath, new String[]{folderPath, fileName});
                adapter.getData().get(selectItem).setDanmuPath(danmuPath);
                adapter.notifyItemChanged(selectItem);
            }
        }
    }
}
