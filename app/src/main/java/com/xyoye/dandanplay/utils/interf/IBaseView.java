package com.xyoye.dandanplay.utils.interf;

/**
 *  Activity, Fragment基类接口
 *
 * Modified by xyoye on 2015/12/2.
 */
public interface IBaseView {

    /**
     * 初始化控件布局
     */
    void initPageView();

    /**
     * 初始化数据
     */
    void initData();

    /**
     * 初始化监听事件
     */
    void initPageViewListener();

}
