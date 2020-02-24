package com.xyoye.dandanplay.utils.net;

import android.app.ProgressDialog;

import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.utils.ResponseError;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyoye on 2020/2/23.
 */

public abstract class CommShooterDataObserver<T> implements Observer<T> {

    private Lifeful lifeful;
    private ProgressDialog progressDialog;
    private Disposable mDisposable;

    public CommShooterDataObserver() {
        lifeful = null;
    }

    public CommShooterDataObserver(Lifeful lifeful) {
        this.lifeful = lifeful;
    }

    public CommShooterDataObserver(Lifeful lifeful, ProgressDialog progressDialog) {
        this.lifeful = lifeful;
        this.progressDialog = progressDialog;
        progressDialog.setOnCancelListener(dialog -> mDisposable.dispose());
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        if (lifeful != null && !lifeful.isAlive()) {
            d.dispose();
        }
    }

    @Override
    public void onNext(T value) {
        if (lifeful == null || lifeful.isAlive()) {
            onSuccess(value);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (lifeful == null || lifeful.isAlive()) {
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

    public abstract void onSuccess(T t);

    public abstract void onError(int errorCode, String message);
}