package com.xyoye.core.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xyoye.core.interf.presenter.BasePresenter;


/**
 * Created by yzd on 2017/2/5 0005.
 */

public abstract class BaseMvpActivity<T extends BasePresenter> extends BaseAppCompatActivity {

    protected T presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        presenter = initPresenter();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        super.init();
        presenter.init();
    }

    @Override
    public void initPageView() {
        presenter.initPage();
    }

    @Override
    public void initPageViewListener() {

    }

    @Override
    protected void process(Bundle savedInstanceState) {
        super.process(savedInstanceState);
        presenter.process(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    protected void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    /**
     * 返回主持人
     */
    protected abstract @NonNull
    T initPresenter();
}
