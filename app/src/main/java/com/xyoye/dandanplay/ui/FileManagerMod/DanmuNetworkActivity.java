package com.xyoye.dandanplay.ui.FileManagerMod;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.DanmuNetworkPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuNetworkActivity extends BaseActivity<DanmuNetworkPresenter> implements DanmuNetworkView{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @Override
    public void initView() {
        setTitle("");
        toolbarTitle.setText("选择网络弹幕");
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected DanmuNetworkPresenter initPresenter() {
        return new DanmuNetworkPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_danmu_network;
    }
}
