package com.xyoye.dandanplay.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.interf.IAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

/**
 * RecyclerView 通用Adapter
 * 需要不同Item显示的，请重写getItemViewType(int position);
 * 建议用枚举类定义Item的类型
 * <p>
 * Modified by xyoye on 2019/5/27.
 */
public abstract class BaseRvAdapter<T> extends RecyclerView.Adapter implements IAdapter<T> {

    private List<T> mData;

    protected BaseRvAdapter(List<T> data) {
        if (data == null) data = new ArrayList<>();
        this.mData = data;
    }

    public BaseRvAdapter() {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RcvAdapterItemViewHolder(parent.getContext(), parent, onCreateItem(viewType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < mData.size()) {
            ((RcvAdapterItemViewHolder) holder).getItem().onUpdateViews(mData.get(position), position);
        } else {
            ((RcvAdapterItemViewHolder) holder).getItem().onUpdateViews(null, position);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public void setData(@NonNull List<T> data) {
        //this.mData.clear();
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public List<T> getData() {
        return mData;
    }

    public class RcvAdapterItemViewHolder extends RecyclerView.ViewHolder {

        private AdapterItem<T> mItem;

        protected RcvAdapterItemViewHolder(Context context, ViewGroup parent, AdapterItem<T> item) {
            super(LayoutInflater.from(context).inflate(item.getLayoutResId(), parent, false));
            this.mItem = item;
            ButterKnife.bind(item, itemView);
            this.mItem.initItemViews(itemView);
            this.mItem.onSetViews();
        }

        public AdapterItem<T> getItem() {
            return mItem;
        }

    }

    @Override
    public void addItem(int position, T model) {
        if (position < mData.size()) {
            mData.add(position, model);
            notifyItemInserted(position);
        }
    }

    @Override
    public void addItem(T model) {
        int position = mData.size();
        mData.add(model);
        notifyItemInserted(position);
    }

    @Override
    public T removeItem(int position) {
        if (position < mData.size()) {
            T model = mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mData.size());
            return model;
        }
        return null;
    }

    @Override
    public boolean removeItem(T model) {
        int position = mData.indexOf(model);
        boolean isSuccess = mData.remove(model);
        if (isSuccess) {
            notifyItemRemoved(position);
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
        notifyItemRangeInserted(mData.size() - 1, data.size());
    }

    @Override
    public void addAll(int position, List<T> data) {
        mData.addAll(position, data);
        notifyItemRangeInserted(position, data.size());
    }

    @Override
    public void removeSubList(int startPosition, int count) {
        int endPosition = startPosition + count;
        if (endPosition < mData.size()) {
            mData.subList(startPosition, endPosition).clear();
            notifyItemRangeRemoved(startPosition, count);
        }
    }

    @Override
    public void swap(int fromPosition, int toPosition) {
        Collections.swap(mData, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (fromPosition < toPosition) {
            notifyItemRangeChanged(fromPosition, getItemCount() - fromPosition);
        } else {
            notifyItemRangeChanged(toPosition, getItemCount() - toPosition);
        }
    }

}
