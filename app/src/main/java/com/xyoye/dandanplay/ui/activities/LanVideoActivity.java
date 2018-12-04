package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.mvp.impl.LanVideoPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LanVideoPresenter;
import com.xyoye.dandanplay.mvp.view.LanVideoView;

import java.util.List;

import butterknife.BindView;

/**
 * Created by xyy on 2018/11/22.
 */

public class LanVideoActivity extends BaseMvpActivity<LanVideoPresenter> implements LanVideoView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private BaseRvAdapter<VideoBean> adapter;
    private List<VideoBean> videoBeans;

    @NonNull
    @Override
    protected LanVideoPresenter initPresenter() {
        return new LanVideoPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.acitivity_lan_video;
    }

    @Override
    public void initView() {
        String title = getIntent().getStringExtra("lan_folder_title");
        setTitle(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
    }

    @Override
    public void initListener() {

    }
}
