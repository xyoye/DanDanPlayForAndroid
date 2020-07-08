package com.xyoye.dandanplay.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.SkinAppCompatDelegateImpl;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.gyf.immersionbar.ImmersionBar;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.IBaseView;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * AppCompatActivity基类
 * Activity生命周期
 * onCreate()
 * onStart()
 * onResume()
 * onPause()
 * onStop()
 * onDestroy()
 * <p>
 * Modified by xyoye on 2019/5/27.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements IBaseView {

    private Toolbar mActionBarToolbar;

    protected boolean isDestroyed = false;

    private Handler handler;

    private Unbinder unbind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBeforeSetContentLayout();
        setContentView(initPageLayoutID());
        unbind = ButterKnife.bind(this);
        initActionBar();
        setStatusBar();
        init();
        initPageView();
        initPageViewListener();
        process(savedInstanceState);
    }

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        //换肤支持
        return SkinAppCompatDelegateImpl.get(this, this);
    }

    /**
     * 返回主布局id
     */
    @LayoutRes
    protected abstract int initPageLayoutID();

    /**
     * 初始化toolbar
     */
    @SuppressLint("PrivateResource")
    protected void initActionBar() {
        if (getActionBarToolbar() == null) {
            return;
        }
        mActionBarToolbar.setBackgroundColor(getToolbarColor());
        if (hasBackActionbar() && getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            setSupportActionBar(mActionBarToolbar);
            mActionBarToolbar.setNavigationOnClickListener(v -> finishPage());
        }
    }

    /**
     * 获取toolbar对象
     */
    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = findViewById(R.id.toolbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    /**
     * 设置状态栏样式
     */
    protected void setStatusBar() {
        ImmersionBar.with(this)
                .statusBarColorInt(CommonUtils.getResColor(R.color.colorPrimaryDark))
                .fitsSystemWindows(true)
                .init();
    }

    /**
     * 初始化
     */
    protected void init() {
    }

    /**
     * 逻辑处理
     */
    protected void process(Bundle savedInstanceState) {
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
        unbind.unbind();
    }

    /**
     * 设置actionbar返回键
     */
    protected boolean hasBackActionbar() {
        return true;
    }

    /**
     * 设置返回键图标
     */
    @DrawableRes
    protected int setBackIcon() {
        return 0;
    }

    /**
     * 设置toolbar背景颜色
     */
    @ColorInt
    protected int getToolbarColor() {
        return CommonUtils.getResColor(R.color.colorPrimaryDark);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBeforeFinish();
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * 结束页面
     */
    public void finishPage() {
        onBeforeFinish();
        finish();
    }

    /**
     * 初始化布局前回调
     */
    protected void onBeforeSetContentLayout() {
    }

    /**
     * 结束页面前回调
     */
    protected void onBeforeFinish() {

    }

    /**
     * 启动activity
     */
    public void launchActivity(Class<? extends Activity> cls) {
        launchActivity(cls, null);
    }

    /**
     * 带参数启动activity
     */
    public void launchActivity(Class<? extends Activity> cls, @Nullable Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 带参数并设置flag启动activity
     */
    public void launchActivity(Class<? extends Activity> cls, @Nullable Bundle bundle, int flags) {
        Intent intent = new Intent(this, cls);
        intent.setFlags(flags);
        if (bundle != null) intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 带有回调结果启动activity
     */
    public void launchActivityForResult(Class<? extends Activity> cls, int requestCode, @Nullable Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishPage();
        }
        return false;
    }

    @Override
    public void initData() {

    }

    /**
     * 判断当前activity是否被销毁
     */
    public boolean isDestroyed() {
        return super.isDestroyed() || isDestroyed;
    }

    public Handler getHandler() {
        synchronized (this) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
        return handler;
    }

    /**
     * 设置遮罩层灰度
     */
    public void backgroundAlpha(Float bgAlpha) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.getWindow().setAttributes(lp);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
