package com.xyoye.dandanplay.utils.database.callback;

import android.database.Cursor;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * Created by xyoye on 2019/9/18.
 *
 * 数据库查询异步回调，回调查询结果
 */

public interface QueryAsyncResultCallback<T> {

    /**
     * 从Cursor提取需要的数据
     *
     * 在线程池执行
     */
    @NonNull
    T onQuery(@NonNull  Cursor cursor);

    /**
     * 返回处理完成的数据
     *
     * 在主线程执行
     */
    void onResult(@NonNull T result);
}
