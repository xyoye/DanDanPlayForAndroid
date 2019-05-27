package com.xyoye.dandanplay.utils.interf.view;

/**
 * 访问数据处理接口
 *
 * Modified by xyoye on 2017/6/23.
 */
public interface LoadDataView {

    /**
     * 展示加载等待窗
     */
    void showLoading();

    /**
     * 关闭加载等待窗
     */
    void hideLoading();

    /**
     * 错误信息返回
     * @param message
     */
    void showError(String message);
}
