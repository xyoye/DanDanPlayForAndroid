package com.xyoye.core.interf.view;

/**
 * 网络状态处理接口
 * Created by yzd on 2016/7/7.
 */
public interface NetworkView{

    /**
     * 网络正常
     */
    void normalNetwork();

    /**
     * 网络异常
     */
    void networkAnomaly();
}
