package com.xyoye.dandanplay.utils.database.callback;

import android.database.Cursor;

import com.xyoye.dandanplay.utils.Lifeful;

import io.reactivex.annotations.Nullable;

/**
 * Created by xyoye on 2019/9/23.
 */

public abstract class QueryAsyncResultCallback<T> {

    private Lifeful lifeful;

    public QueryAsyncResultCallback(Lifeful lifeful){
        this.lifeful = lifeful;
    }

    /**
     * 从Cursor提取需要的数据
     *
     * 在线程池执行
     */
    public abstract T onQuery(@Nullable Cursor cursor);

    /**
     * 返回结果前预处理
     *
     * 检查生命周期
     */
    public void onPrepared(@Nullable T result){
        if (lifeful != null && lifeful.isAlive()){
            onResult(result);
        }
    }

    /**
     * 返回处理完成的数据
     *
     * 在主线程执行
     */
    public abstract void onResult(@Nullable T result);
}
