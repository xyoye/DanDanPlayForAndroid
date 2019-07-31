package com.xyoye.dandanplay.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.interf.IBaseView;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment基类
 * Fragment的生命周期如下（附加对应Activity的生命周期状态）
 * Fragment ---------------- Activity
 * ----------------------------------
 * onInflate()              | before Create
 * ----------------------------------
 * onAttach()               |
 * onCreate()               |
 * onCreateView()           | Created
 * onViewCreated()          |
 * onActivityCreated()      |
 * ----------------------------------
 * onStart()                | Started
 * ----------------------------------
 * onResume()               | Resumed
 * ----------------------------------
 * onPause()                | Paused
 * ----------------------------------
 * onStop()                 | Stopped
 * ----------------------------------
 * onDestroyView()          |
 * onDestroy()              | Destroyed
 * onDetach()               |
 * ----------------------------------
 *
 * Modified by xyoye on 2019/5/27.
 */
public abstract class BaseAppFragment extends Fragment implements IBaseView, Lifeful  {

    public static final String TAG = BaseAppFragment.class.getSimpleName();

    protected boolean isActivityCreated = false; // 页面控件是否已初始化

    public boolean isFirstVisible = true; // 是否第一次可见

    protected View mFragmentView;

    protected int position;

    protected boolean mIsVisibleToUser = false;

    protected BaseAppCompatActivity mContext;

    private Unbinder unbind;

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        Log.d(TAG,"setUserVisibleHint====>"+isVisibleToUser+"====>"+position);
        if (isActivityCreated) {
            if (isVisibleToUser) {
                if (isFirstVisible) {
                    isFirstVisible = false;
                    onPageFirstVisible();
                }
                onPageStart();
            } else {
                onPageEnd();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreate====>"+position);
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        mContext = (BaseAppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView====>"+position+"====>"+isActivityCreated+"====>"+isFirstVisible);
        return inflater.inflate(initPageLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onViewCreated====>"+position);
        mFragmentView = view;
        unbind = ButterKnife.bind(this, mFragmentView);
        initPageView();
        initPageViewListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onActivityCreated====>"+position+"_"+getUserVisibleHint());
        super.onActivityCreated(savedInstanceState);
        isActivityCreated = true;
        if (getUserVisibleHint() || mIsVisibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                onPageFirstVisible();
                onPageStart();
            }
        }
        process(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG,"onDestroyView====>"+position);
        super.onDestroyView();
        unbind.unbind();
        mFragmentView = null;
    }

    @Override
    public void onDetach() {
        Log.d(TAG,"onDetach====>"+position);
        super.onDetach();
        //noinspection TryWithIdenticalCatches
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @LayoutRes
    protected abstract int initPageLayoutId();

    @Override
    public boolean getUserVisibleHint() {
        Log.d(TAG,"getUserVisibleHint====>"+super.getUserVisibleHint()+"====>"+position);
        return super.getUserVisibleHint();
    }

    /**
     * 当页面首次可见时调用。调用时页面控件已经完成初始化
     * 用于ViewPager下的页面懒加载，在一个生命周期内只会调用一次
     */
    protected void onPageFirstVisible() {
        Log.d(TAG,"onPageFirstVisible====>"+position);
    }

    /**
     * 逻辑处理
     */
    protected void process(Bundle savedInstanceState) {
        Log.d(TAG,"process====>"+position);
    }

    protected void onPageStart() {
        Log.d(TAG,"onPageStart====>"+super.getUserVisibleHint()+"====>"+position);
        lazyLoad();
    }

    protected void onPageEnd() {
        Log.d(TAG,"onPageEnd====>"+super.getUserVisibleHint()+"====>"+position);
    }

    /**
     * 懒加载数据
     */
    protected void lazyLoad() {

    }

    /**
     * 跳转到新的页面
     */
    protected void launchActivity(Class<? extends Activity> cls) {
        launchActivity(cls, null);
    }

    protected void launchActivity(Class<? extends Activity> cls, @Nullable Bundle bundle) {
        Intent intent = new Intent(this.getActivity(), cls);
        if (bundle != null)
            intent.putExtras(bundle);
        startActivity(intent);
    }

    protected void launchActivity(Class<? extends Activity> cls, @Nullable Bundle bundle, Integer flag) {
        Intent intent = new Intent(this.getActivity(), cls);
        if (flag != null) {
            intent.setFlags(flag);
        }
        if (bundle != null)
            intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void initData() {

    }

    public BaseAppCompatActivity getBaseActivity() {
        return mContext != null ? mContext : (BaseAppCompatActivity) this.getContext();
    }

    @Override
    public boolean isAlive() {
        return activityIsAlive();
    }

    public boolean activityIsAlive() {
        Activity currentActivity = getActivity();
        if (currentActivity == null) return false;
        return !(currentActivity.isDestroyed() || currentActivity.isFinishing());
    }
}
