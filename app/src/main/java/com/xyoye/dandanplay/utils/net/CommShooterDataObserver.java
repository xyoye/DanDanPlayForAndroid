package com.xyoye.dandanplay.utils.net;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.utils.net.utils.ResponseError;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyoye on 2020/2/23.
 */

public abstract class CommShooterDataObserver<T> implements Observer<T> {

    private LifecycleOwner lifecycleOwner;
    private ProgressDialog progressDialog;
    private Disposable mDisposable;

    public CommShooterDataObserver() {
        lifecycleOwner = null;
    }

    public CommShooterDataObserver(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public CommShooterDataObserver(LifecycleOwner lifecycleOwner, ProgressDialog progressDialog) {
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
            onSuccess(value);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (isNotDestroyed()) {
            if (throwable instanceof ResponseError) {
                //网络请求异常
                ResponseError error = (ResponseError) throwable;
                onError(error.code, error.message);
            } else {
                //程序代码异常
                onError(-999, throwable.getMessage());
            }
        }
    }

    @Override
    public void onComplete() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean isNotDestroyed() {
        return lifecycleOwner == null ||
                lifecycleOwner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED;
    }

    public abstract void onSuccess(T t);

    public abstract void onError(int errorCode, String message);
}