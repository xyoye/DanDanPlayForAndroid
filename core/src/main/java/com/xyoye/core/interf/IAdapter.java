package com.xyoye.core.interf;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by admin on 2016/6/21.
 * Adapter方法接口
 */
public interface IAdapter<T> {

    /**
     * 设置数据列表
     *
     * @param data 数据列表
     */
    void setData(@NonNull List<T> data);

    /**
     * 获得数据列表
     */
    List<T> getData();

    /**
     * 返回指定位置的model
     *
     * @param position item的位置
     * @return model
     */
    T getItem(int position);

    /**
     * 当缓存中无法得到所需item时才会调用
     */
    @NonNull
    AdapterItem<T> onCreateItem(int viewType);

    /**
     * 根据指定位置position,添加model
     *
     * @param position 插入的位置
     * @param model    单个model
     */
    void addItem(int position, T model);

    /**
     * 在数据列尾添加model
     *
     * @param model 单个model
     */
    void addItem(T model);

    /**
     * 根据指定位置position,删除的model
     *
     * @param position 删除位置
     * @return model 返回model，若model不为空，则删除成功，反之失败
     */
    T removeItem(int position);


    /**
     * 根据model，删除Item
     *
     * @param model 单个数据model
     * @return boolean 是否删除成功 若true，则删除成功，反之失败
     */
    boolean removeItem(T model);

    /**
     * 清空List的数据
     */
    void itemsClear();

    /**
     * 在数据列后面,添加一组model集合
     *
     * @param data model的集合
     */
    void addAll(List<T> data);

    /**
     * 根据指定位置，添加一组model集合
     *
     * @param position 插入的位置
     * @param data     model的集合
     */
    void addAll(int position, List<T> data);

    /**
     * 删除指定位置段的Item
     *
     * @param startPosition 开始的位置
     * @param count         多少个
     */
    void removeSubList(int startPosition, int count);

    /**
     * 交换两个Item的位置
     *
     * @param fromPosition 交换的位置
     * @param toPosition   交换的位置
     */
    void swap(int fromPosition, int toPosition);
}
