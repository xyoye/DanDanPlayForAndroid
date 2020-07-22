package com.xyoye.dandanplay.utils.database.callback;

import android.database.Cursor;

import androidx.annotation.NonNull;

/**
 * Created by xyoye on 2019/9/18.
 *
 * 数据库查询同步回调
 */
public interface QuerySyncCallback {

    void onQuery(@NonNull Cursor cursor);
}