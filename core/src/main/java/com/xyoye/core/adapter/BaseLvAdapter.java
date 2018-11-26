package com.xyoye.core.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.interf.IAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by xyy on 2017/9/15
 * ListView 通用Adapter
 * 需要不同Item显示的，请重写getItemViewType(int position);
 * 建议用枚举类定义Item的类型
 */
public abstract class BaseLvAdapter<T> extends BaseAdapter implements IAdapter<T> {

    private List<T> mData;

    private LayoutInflater mInflater;

    protected BaseLvAdapter( List<T> mData) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        this.mData = mData;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        AdapterItem<T> item;
        if (convertView == null) {
            item = onCreateItem(getItemViewType(position));
            convertView = mInflater.inflate(item.getLayoutResId(), parent, false);
            convertView.setTag(item);
            ButterKnife.bind(item, convertView);
            item.initItemViews(convertView);
            item.onSetViews();
        } else {
            //noinspection unchecked
            item = (AdapterItem<T>) convertView.getTag();
        }
        if (position < mData.size()) {
            item.onUpdateViews(mData.get(position), position);
        } else {
            item.onUpdateViews(null, position);
        }
        return convertView;
    }

    @Override
    public void setData(@NonNull List<T> data) {
       // this.mData.clear();
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public List<T> getData() {
        return mData;
    }

    @Override
    public void addItem(int position, T model) {
        if (position < mData.size()) {
            mData.add(position, model);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addItem(T model) {
        mData.add(model);
        notifyDataSetChanged();
    }

    @Override
    public T removeItem(int position) {
        if (position < mData.size()) {
            T model = mData.remove(position);
            notifyDataSetChanged();
            return model;
        }
        return null;
    }

    @Override
    public boolean removeItem(T model) {
        boolean isSuccess = mData.remove(model);
        if (isSuccess) {
            notifyDataSetChanged();
        }
        return isSuccess;
    }

    @Override
    public void itemsClear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public void addAll(List<T> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(int position, List<T> data) {
        mData.addAll(position, data);
        notifyDataSetChanged();
    }

    @Override
    public void removeSubList(int startPosition, int count) {
        int endPosition = startPosition + count;
        if (endPosition < mData.size()) {
            mData.subList(startPosition, endPosition).clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void swap(int fromPosition, int toPosition) {
        Collections.swap(mData, fromPosition, toPosition);
        notifyDataSetChanged();
    }

}
