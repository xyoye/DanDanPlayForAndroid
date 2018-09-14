package com.xyoye.dandanplay.ui.folderMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
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
import com.xyoye.dandanplay.ui.danmuMod.DanmuNetworkActivity;
import com.xyoye.dandanplay.ui.playMod.PlayerActivity;
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
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    public final static int SELECT_NETWORK_DANMU = 104;
    public final static int OPEN_VIDEO = 102;
    private int selectItem = -1;
    private int openVideoPosition = -1;

    private BaseRvAdapter<VideoBean> adapter;
    private List<VideoBean> videoBeans;

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

    }

    @Override
    public void hideLoading() {

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
        //未设置弹幕情况下，自动匹配同名弹幕(先匹配相同目录下，再匹配默认下载目录下)
        if (StringUtils.isEmpty(videoBean.getDanmuPath())){
            String path = videoBean.getVideoPath();
            String danmuPath = path.substring(0, path.lastIndexOf("."))+ ".xml";
            File file = new File(danmuPath);
            if (file.exists())
                videoBean.setDanmuPath(danmuPath);
            else {
                String ext = FileUtils.getFileExtension(path);
                String name = FileUtils.getFileName(path).replace(ext, "xml");
                danmuPath = AppConfigShare.getInstance().getDownloadFolder()+ "/" + name;
                file = new File(danmuPath);
                if (file.exists())
                    videoBean.setDanmuPath(danmuPath);
            }
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
        Intent intent = new Intent(FolderActivity.this, DanmuNetworkActivity.class);
        intent.putExtra("path", event.getVideoPath());
        startActivityForResult(intent, SELECT_NETWORK_DANMU);
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
            if (requestCode == SELECT_NETWORK_DANMU){
                String danmuPath = data.getStringExtra("path");
                String videoPath = adapter.getData().get(selectItem).getVideoPath();
                String folderPath = FileUtils.getDirName(videoPath);
                String fileName = FileUtils.getFileName(videoPath);
                presenter.updateDanmu(danmuPath, new String[]{folderPath, fileName});
                adapter.getData().get(selectItem).setDanmuPath(danmuPath);
                adapter.notifyItemChanged(selectItem);
            }
        }
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
