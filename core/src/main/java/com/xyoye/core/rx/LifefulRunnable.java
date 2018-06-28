package com.xyoye.core.rx;

/**
 * Created by yzd on 2017/4/8 0008.
 * <p>
 * 与周期相关的异步线程回调类。
 */

public class LifefulRunnable implements Runnable {

    private LifefulGenerator<Runnable> mLifefulGenerator;

    public LifefulRunnable(Runnable runnable, Lifeful lifeful) {
        mLifefulGenerator = new DefaultLifefulGenerator<>(runnable, lifeful);
    }

    @Override
    public void run() {
        if (LifefulUtils.shouldGoHome(mLifefulGenerator)) {
            return;
        }
        mLifefulGenerator.getCallback().run();
    }
}
