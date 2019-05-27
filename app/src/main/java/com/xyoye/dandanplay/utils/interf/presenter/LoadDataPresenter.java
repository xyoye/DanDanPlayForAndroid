package com.xyoye.dandanplay.utils.interf.presenter;

/**
 * 数据接口
 *
 * Modified by xyoye on 2017/6/23.
 */
public interface LoadDataPresenter {

    /**
     * 获取初始数据
     */
    void getInitialData();

    /**
     * 刷新数据
     */
    void refreshData();

    /**
     * 获取下一页数据
     */
    void getNextData();
}
