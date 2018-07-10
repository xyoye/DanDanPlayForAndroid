package com.xyoye.core.rx;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/7/9.
 */



public class RxUtils {
    public RxUtils() {
    }

    public static <T> LifecycleTransformer<T> bindToLifecycle(Context context) {
        if(context instanceof LifecycleProvider) {
            return ((LifecycleProvider)context).bindToLifecycle();
        } else {
            throw new IllegalArgumentException("context not the LifecycleProvider type");
        }
    }

    public static <T> LifecycleTransformer<T> bindToLifecycle(Fragment lifecycle) {
        if(lifecycle instanceof LifecycleProvider) {
            return ((LifecycleProvider)lifecycle).bindToLifecycle();
        } else {
            throw new IllegalArgumentException("fragment not the LifecycleProvider type");
        }
    }

    public static <T, C> ObservableTransformer bindToLifecycle(C lifecycle) {
        return (lifecycle != null && lifecycle instanceof LifecycleProvider)
                ? ((LifecycleProvider)lifecycle).bindToLifecycle()
                : (upstream) -> upstream;
    }

    public static <T> ObservableTransformer<T, T> bindToLifecycle(LifecycleProvider lifecycle) {
        return (lifecycle != null)
                ? lifecycle.bindToLifecycle()
                : (upstream) -> upstream;
    }

    public static <T> ObservableTransformer<T, T> schedulersTransformer() {
        return (observable) -> {
            return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        };
    }

    public static <T> ObservableTransformer<T, T> schedulersNewTransformer() {
        return (observable) -> {
            return observable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        };
    }
}
