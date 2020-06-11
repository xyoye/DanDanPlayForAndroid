package com.xyoye.dandanplay.utils.net;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.JsonSyntaxException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Modified by xyoye on 2017/5/15
 */

public abstract class CommJsonNoDataObserver implements Observer<CommJsonEntity> {

    private LifecycleOwner lifecycleOwner;
    private ProgressDialog progressDialog;
    private Disposable mDisposable;

    public CommJsonNoDataObserver() {
        lifecycleOwner = null;
    }

    public CommJsonNoDataObserver(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public CommJsonNoDataObserver(LifecycleOwner lifecycleOwner, ProgressDialog progressDialog) {
        this.lifecycleOwner = lifecycleOwner;
        this.progressDialog = progressDialog;
        progressDialog.setOnCancelListener(dialog -> mDisposable.dispose());
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        if (!isNotDestroyed()) {
            d.dispose();
        }
    }

    @Override
    public void onNext(CommJsonEntity value) {
        if (isNotDestroyed()) {
            if (value.isSuccess()) {
                onSuccess();
            } else {
                onError(value.getErrorCode(), value.getErrorMessage());
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        if (isNotDestroyed()) {
            onError(-1, getErrorMessage(e));
        }
    }

    @Override
    public void onComplete() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private String getErrorMessage(Throwable e) {
        LogUtils.e(e.toString());
        if (e instanceof JsonSyntaxException) {
            LogUtils.i("error", e.toString());
            return "数据异常";
        } else if (e instanceof UnknownHostException) {
            LogUtils.i("error", e.toString());
            return "网络连接中断";
        } else if (e instanceof SocketTimeoutException) {
            return "服务器繁忙";
        }
        return "服务器繁忙";
    }

    private boolean isNotDestroyed() {
        return lifecycleOwner == null ||
                lifecycleOwner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED;
    }

    public abstract void onSuccess();

    public abstract void onError(int errorCode, String message);
}
