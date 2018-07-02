package com.xyoye.dandanplay.ui.folderMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.PixelUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.event.OpenFolderEvent;
import com.xyoye.dandanplay.event.OpenVideoEvent;
import com.xyoye.dandanplay.mvp.impl.FolderPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.ui.temp.VideoViewActivity;
import com.xyoye.dandanplay.weight.decorator.SpacesItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class FolderActivity extends BaseActivity<FolderPresenter> implements FolderView{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ptr_pull_rv_to_refresh)
    PtrFrameLayout refresh;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private BaseRvAdapter<VideoBean> adapter;

    @Override
    public void initView() {
        setTitle("");
        String title = getIntent().getStringExtra(OpenFolderEvent.FOLDERTITLE);
        toolbarTitle.setText(title);

        StoreHouseHeader header = new StoreHouseHeader(this);
        header.setPadding(0, PixelUtil.dip2px(this, 20) , 0, PixelUtil.dip2px(this, 20));
        header.initWithString("dan dan player");
        header.setTextColor(this.getResources().getColor(R.color.theme_color));
        refresh.disableWhenHorizontalMove(true);
        refresh.setDurationToCloseHeader(1500);
        refresh.setHeaderView(header);
        refresh.addPtrUIHandler(header);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1,0,0,0));

        showLoading();
        presenter.refreshVideos();
    }

    @Override
    public void initListener() {
        refresh.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                presenter.refreshVideos();
            }
        });
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
        refresh.refreshComplete();
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
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getToolbarColor(),0);
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
        VideoBean videoBean = event.getBean();
        Intent intent = new Intent(this, VideoViewActivity.class);
        intent.putExtra("file_title", videoBean.getVideoName());
        intent.putExtra("path",videoBean.getVideoPath());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openDanmuSetting(OpenDanmuSettingEvent event){
        // TODO: 2018/7/1 弹幕设置页面
        ToastUtils.showShort("弹幕设置正在建造中...");
    }
}
