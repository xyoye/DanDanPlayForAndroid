package com.xyoye.dandanplay.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.blankj.utilcode.util.LogUtils;
import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.interf.IBaseView;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportActivityDelegate;
import me.yokeyword.fragmentation.SupportHelper;
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * AppCompatActivity基类
 * Activity生命周期
 * onCreate()
 * onStart()
 * onResume()
 * onPause()
 * onStop()
 * onDestroy()
 * Created by xyy on 2017/5/18.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements IBaseView, Lifeful, ISupportActivity {

    public static final String TAG = BaseAppCompatActivity.class.getSimpleName();

    public static final String EXTRA_TITLE = "actionbar_title";

    private Toolbar mActionBarToolbar;

    protected boolean isDestroyed = false;

    private Handler handler;

    private Unbinder unbind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtils.d("Activity", "onCreate");
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
        mDelegate.onCreate(savedInstanceState);
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
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishPage();
                }
            });
        }
    }

    /**
     * 获取toolbar对象
     * @return
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
        StatusBarUtil.setColor(this, getToolbarColor());
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
        LogUtils.d("Activity", "onDestroy");
        isDestroyed = true;
        mDelegate.onDestroy();
        super.onDestroy();
        unbind.unbind();
    }

    /**
     * 设置actionbar返回键
     *
     * @return
     */
    protected boolean hasBackActionbar() {
        return true;
    }

    /**
     * 设置返回键图标
     *
     * @return
     */
    @DrawableRes
    protected int setBackIcon() {
        return 0;
    }

    /**
     * 设置toolbar背景颜色
     * @return
     */
    @ColorInt
    protected int getToolbarColor() {
        return this.getResources().getColor(R.color.colorPrimary);
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
     *
     * @param cls
     */
    public void launchActivity(Class<? extends Activity> cls) {
        launchActivity(cls, null);
    }

    /**
     * 带参数启动activity
     * @param cls
     * @param bundle
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
     * @param cls
     * @param bundle
     */
    public void launchActivity(Class<? extends Activity> cls, @Nullable Bundle bundle, int flags) {
        Intent intent = new Intent(this, cls);
        intent.setFlags(flags);
        if (bundle != null) intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 带有回调结果启动activity
     * @param cls
     * @param requestCode
     * @param bundle
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
     *
     * @return
     */
    public boolean isDestroyed() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && super.isDestroyed()) || isDestroyed;
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
     * @param bgAlpha
     */
    public void backgroundAlpha(Float bgAlpha) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.getWindow().setAttributes(lp);
    }

    @Override
    public boolean isAlive() {
        return activityIsAlive();
    }

    public boolean activityIsAlive() {
        if (getBaseContext() == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !(this.isDestroyed() || this.isFinishing());
        } else {
            return !this.isFinishing();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d("Activity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d("Activity", "onPause");
    }


    final public SupportActivityDelegate mDelegate = new SupportActivityDelegate(this);

    @Override
    public SupportActivityDelegate getSupportDelegate() {
        return mDelegate;
    }

    @Override
    public ExtraTransaction extraTransaction() {
        return  mDelegate.extraTransaction();
    }

    @Override
    public FragmentAnimator getFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDelegate.onPostCreate(savedInstanceState);
    }

    @Override
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        mDelegate.setFragmentAnimator(fragmentAnimator);
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    @Override
    public void post(Runnable runnable) {
        mDelegate.post(runnable);
    }

    @Override
    public void onBackPressedSupport() {

    }

    public void loadRootFragment(int containerId, @NonNull ISupportFragment toFragment) {
        mDelegate.loadRootFragment(containerId, toFragment);
    }

    public <F extends ISupportFragment> F findFragment(Class<F> fragmentClass) {
        return SupportHelper.findFragment(getSupportFragmentManager(), fragmentClass);
    }
}
