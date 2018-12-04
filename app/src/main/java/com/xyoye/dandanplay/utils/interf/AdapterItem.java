package com.xyoye.dandanplay.utils.interf;

import android.support.annotation.LayoutRes;
import android.view.View;


/**
 * Created by admin on 2016/6/21.
 * AdapterItem方法接口
 */
public interface AdapterItem<T> {

    /**
     * @return item资源布局文件的id
     */
    @LayoutRes
    int getLayoutResId();

    /**
     * 初始化View
     *
     * @param itemView 布局文件展开的View
     */
    void initItemViews(final View itemView);

    /**
     * 设置View的参数
     */
    void onSetViews();

    /**
     * 根据数据来设置item的内部views
     *
     * @param model    数据list内部的model
     * @param position 当前adapter调用item的位置
     */
    void onUpdateViews(T model, int position);

}
