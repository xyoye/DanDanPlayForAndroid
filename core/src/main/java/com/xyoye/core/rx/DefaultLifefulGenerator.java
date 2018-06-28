package com.xyoye.core.rx;

import java.lang.ref.WeakReference;

/**
 * Created by yzd on 2017/4/8 0008.
 * <p>
 * 默认生命周期管理包装生成器。
 */

public class DefaultLifefulGenerator<T> implements LifefulGenerator<T> {

    private WeakReference<Lifeful> mLifefulWeakReference;
    private boolean mLifefulIsNull;
    private T mCallback;

    public DefaultLifefulGenerator(T callback, Lifeful lifeful) {
        mCallback = callback;
        mLifefulWeakReference = new WeakReference<>(lifeful);
        mLifefulIsNull = lifeful == null;
    }

    @Override
    public T getCallback() {
        return mCallback;
    }

    public WeakReference<Lifeful> getLifefulWeakReference() {
        return mLifefulWeakReference;
    }

    @Override
    public boolean isLifefulNull() {
        return mLifefulIsNull;
    }
}
