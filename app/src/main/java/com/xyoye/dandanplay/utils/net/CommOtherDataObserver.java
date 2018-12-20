package com.xyoye.dandanplay.utils.net;

import android.app.ProgressDialog;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.JsonSyntaxException;
import com.xyoye.dandanplay.utils.Lifeful;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by YE on 2018/7/14.
 */


public abstract class CommOtherDataObserver<T> implements Observer<T>  {
    private Lifeful lifeful;
    private ProgressDialog progressDialog;
    private Disposable mDisposable;

    public CommOtherDataObserver() {
        lifeful = null;
    }

    public CommOtherDataObserver(Lifeful lifeful) {
        this.lifeful = lifeful;
    }

    public CommOtherDataObserver(Lifeful lifeful, ProgressDialog progressDialog) {
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
    public void onError(Throwable e) {
        if (lifeful == null || lifeful.isAlive()) {
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

    public abstract void onSuccess(T t);

    public abstract void onError(int errorCode, String message);

    public void onProgress(int progress, long total){ }
}
