package com.xyoye.dandanplay.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableSubscribeProxy;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.ObservableConverter;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2020/6/19.
 */

public class RxUtils {


    /**
     * 绑定lifecycle, 自动解除订阅
     */
    public static <T> ObservableConverter<T, ObservableSubscribeProxy<T>> bindLifecycle(LifecycleOwner lifecycleOwner) {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, Lifecycle.Event.ON_DESTROY));
    }

    /**
     * IO线程执行任务
     */
    public static <T> ObservableTransformer<T, T> schedulerIO() {
        return observable ->
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 子线程执行任务
     */
    public static <T> ObservableTransformer<T, T> schedulerNew() {
        return observable ->
                observable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread());
    }
}
