package com.xyoye.core.interf.presenter;

/**
 * 数据接口
 * Created by yzd on 2016/7/19.
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
