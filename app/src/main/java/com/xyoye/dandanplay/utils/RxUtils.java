package com.xyoye.dandanplay.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableSubscribeProxy;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.ObservableConverter;

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
}
