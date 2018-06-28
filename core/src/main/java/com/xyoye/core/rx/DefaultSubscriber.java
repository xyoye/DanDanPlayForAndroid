package com.xyoye.core.rx;

import com.xyoye.core.exception.ResponseException;
import com.xyoye.core.utils.TLog;

import org.reactivestreams.Subscriber;

/**
 *
 * Created by yzd on 2016/12/17 0017.
 */

public abstract class DefaultSubscriber<T> implements Subscriber<T> {


    @Override
    public void onError(Throwable e) {
        if (e instanceof ResponseException) {
            ResponseException exception = (ResponseException) e;
            TLog.i("sub_error_code", exception.getErrorCode()+"");
            onError(exception.getErrorCode(), exception.getMessage());
        }
        TLog.i("sub_error", e.getMessage());
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    public abstract void onSuccess(T t);

    public abstract void onError(int errorCode, String message);
}
