package com.xyoye.core.interf.view;

import java.util.List;

/**
 * 数据翻页填充控制
 * Created by yzd on 2016/7/19.
 */
public interface PageControlView<T> {

    /**
     * 初始页数据填充
     * @param data
     */
    void setInitialPageData(List<T> data);

    /**
     * 初始页无数据填充
     */
    void setNoInitialPageData();

    /**
     * 下一页数据填充
     * @param data
     */
    void setNextPageData(List<T> data);

    /**
     * 无下一页数据填充
     */
    void setNoNextPageData();
}
