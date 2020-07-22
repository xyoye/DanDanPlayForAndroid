package com.xyoye.dandanplay.utils.database.callback;

import android.database.Cursor;

import androidx.annotation.Nullable;

/**
 * Created by xyoye on 2019/9/18.
 *
 * 数据库查询同步回调，返回参数
 */
public interface QuerySyncResultCallback<T> {

    T onQuery(@Nullable Cursor cursor);
}