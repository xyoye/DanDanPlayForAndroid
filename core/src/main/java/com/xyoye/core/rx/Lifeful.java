package com.xyoye.core.rx;

/**
 * Created by yzd on 2017/4/8 0008.
 *
 * 判断生命周期是否已经结束的一个接口。
 */

public interface Lifeful {
    /**
     * 判断某一个组件生命周期是否已经走到最后。一般用于异步回调时判断Activity或Fragment生命周期是否已经结束。
     *
     * @return
     */
    boolean isAlive();
}
