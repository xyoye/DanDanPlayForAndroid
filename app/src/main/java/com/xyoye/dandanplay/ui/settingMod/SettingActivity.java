package com.xyoye.dandanplay.ui.settingMod;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.SettingPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SettingPresenter;
import com.xyoye.dandanplay.mvp.view.SettingView;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */


public class SettingActivity extends BaseActivity<SettingPresenter> implements SettingView {
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @Override
    public void initView() {
        setTitle("");
        toolbarTitle.setText("设置");
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected SettingPresenter initPresenter() {
        return new SettingPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_setting;
    }
}
