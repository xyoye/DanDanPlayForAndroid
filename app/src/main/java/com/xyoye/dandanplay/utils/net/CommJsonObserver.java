package com.xyoye.dandanplay.utils.net;

import android.app.ProgressDialog;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.JsonSyntaxException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Modified by xyoye on 2017/5/15
 */

public abstract class CommJsonObserver<T extends CommJsonEntity> implements Observer<T> {

    private LifecycleOwner lifecycleOwner;
    private ProgressDialog progressDialog;
    private Disposable mDisposable;

    public CommJsonObserver() {
        lifecycleOwner = null;
    }

    public CommJsonObserver(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public CommJsonObserver(LifecycleOwner lifecycleOwner, ProgressDialog progressDialog) {
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
    public void onNext(T value) {
        if (isNotDestroyed()) {
            if (value.isSuccess()) {
                onSuccess(value);
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

    public abstract void onSuccess(T t);

    public abstract void onError(int errorCode, String message);
}
