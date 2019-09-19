package com.xyoye.dandanplay.utils.database.callback;

import android.database.Cursor;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * Created by xyoye on 2019/9/18.
 *
 * 数据库查询异步回调，只异步执行对Cursor的处理
 */

public interface QueryAsyncCallback {

    /**
     * 从Cursor提取需要的数据
     *
     * 在线程池执行
     */
    void onQuery(@NonNull Cursor cursor);
}
