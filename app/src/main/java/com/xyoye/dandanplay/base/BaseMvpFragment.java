package com.xyoye.dandanplay.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xyoye.dandanplay.ui.weight.dialog.BaseLoadingDialog;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Modified by xyoye on 2019/5/27.
 */

public abstract class BaseMvpFragment<T extends BaseMvpPresenter> extends BaseAppFragment {

    protected T presenter;
    protected Dialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialog = null;
        presenter.destroy();
    }

    public void showLoadingDialog() {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new BaseLoadingDialog(mContext);
            dialog.show();
        }
    }

    public void showLoadingDialog(String msg) {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new BaseLoadingDialog(mContext, msg);
            dialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * 返回主持人
     */
    protected abstract @NonNull
    T initPresenter();
}
