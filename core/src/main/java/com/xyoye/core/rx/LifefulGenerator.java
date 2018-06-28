package com.xyoye.core.rx;

import java.lang.ref.WeakReference;

/**
 * Created by yzd on 2017/4/8 0008.
 *
 * 生命周期具体对象生成器。
 */

public interface LifefulGenerator<T> {

    /**
     * @return 返回回调接口。
     */
    T getCallback();
    /**
     * 获取与生命周期绑定的弱引用，一般为Context，使用一层WeakReference包装。
     *
     * @return 返回与生命周期绑定的弱引用。
     */
    WeakReference<Lifeful> getLifefulWeakReference();
    /**
     * 传入的引用是否为Null。
     *
     * @return true if {@link Lifeful} is null.
     */
    boolean isLifefulNull();
}
