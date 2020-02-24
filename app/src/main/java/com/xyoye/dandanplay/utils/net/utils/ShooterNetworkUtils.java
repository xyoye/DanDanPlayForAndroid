package com.xyoye.dandanplay.utils.net.utils;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterNetworkUtils {
    /**
     * 网络请求转换器
     *
     * 执行在IO线程
     * 回调到Main线程
     * 添加错误处理器
     */
    public static <T> ObservableTransformer<T, T> network() {
        return observable ->
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorResumeNext((Function<Throwable, Observable<T>>) t -> {
                            return Observable.error(ResponseErrorHandle.handleError(t));
                        });
    }
}
