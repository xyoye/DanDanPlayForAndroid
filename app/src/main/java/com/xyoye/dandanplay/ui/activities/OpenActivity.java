package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.mvp.impl.OpenPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.OpenPresenter;
import com.xyoye.dandanplay.mvp.view.OpenView;
import com.xyoye.dandanplay.ui.activities.personal.LoginActivity;

/**
 * Created by xyoye on 2018/7/15.
 */

public class OpenActivity extends BaseMvpActivity<OpenPresenter> implements OpenView {

    @Override
    protected void process(Bundle savedInstanceState) {
        super.process(savedInstanceState);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_open;
    }

    @Override
    public void initView() {
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        Window window = getWindow();
        window.setFlags(flag, flag);
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected OpenPresenter initPresenter() {
        return new OpenPresenterImpl(this, this);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this, null);
        StatusBarUtil.hideFakeStatusBarView(this);
    }

    @Override
    public void launch(boolean toLogin) {
        if (!toLogin)
            launchActivity(MainActivity.class);
        else {
            Intent intent = new Intent(OpenActivity.this, LoginActivity.class);
            intent.putExtra("isOpen", true);
            startActivity(intent);
        }
        this.finish();
    }

}
