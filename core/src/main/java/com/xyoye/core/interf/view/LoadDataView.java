package com.xyoye.core.interf.view;

/**
 * 访问数据处理接口
 * Created by yzd on 2016/3/18.
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
