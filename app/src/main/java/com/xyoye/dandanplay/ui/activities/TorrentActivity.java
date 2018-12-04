package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.mvp.impl.TorrentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.TorrentPresenter;
import com.xyoye.dandanplay.mvp.view.TorrentView;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentActivity extends BaseMvpActivity<TorrentPresenter> implements TorrentView {

    @NonNull
    @Override
    protected TorrentPresenter initPresenter() {
        return new TorrentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_torrent;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
