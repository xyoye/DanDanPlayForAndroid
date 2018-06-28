package com.xyoye.core.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xyoye.core.R;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.interf.IAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 *
 * Created by yzd on 2016/6/23.
 */
public abstract class CommPagerAdapter<T> extends BasePagerAdapter<View> implements IAdapter<T> {

    private List<T> mDataList;

    private LayoutInflater mInflater;

    private boolean mIsLazy = false;

    public CommPagerAdapter(@Nullable List<T> data) {
        this(data, false);
    }

    public CommPagerAdapter(@Nullable List<T> data, boolean isLazy) {
        if (data == null) {
            data = new ArrayList<>();
        }
        mDataList = data;
        mIsLazy = isLazy;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @NonNull
    @Override
    protected View getViewFromItem(View item, int pos) {
        return item;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View view = super.instantiateItem(container, position);
        if (!mIsLazy) {
            initItem(position, view);
        }
        return view;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, @NonNull Object object) {
        if (mIsLazy && object != currentItem) {
            initItem(position, ((View) object));
        }
        super.setPrimaryItem(container, position, object);
    }

    @SuppressWarnings("unchecked")
    private void initItem(int position, View view) {
        final AdapterItem item = (AdapterItem) view.getTag(R.id.viewPage);
        item.onUpdateViews(mDataList.get(position), position);
    }

    @Override
    protected View createItem(ViewGroup viewPager, int position) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(viewPager.getContext());
        }
        AdapterItem item = onCreateItem(getItemType(position));
        View view = mInflater.inflate(item.getLayoutResId(), null);
        ButterKnife.bind(item, view);
        view.setTag(R.id.viewPage, item);
        item.initItemViews(view);
        item.onSetViews();
        return view;
    }

    public void setIsLazy(boolean isLazy) {
        mIsLazy = isLazy;
    }

    @Override
    protected int getItemType(int position) {
        return super.getItemType(position);
    }

    @Override
    public void setData(@NonNull List<T> data) {
        mDataList = data;
    }

    @Override
    public List<T> getData() {
        return mDataList;
    }

    @Override
    public void addItem(int position, T model) {

    }

    @Override
    public void addItem(T model) {

    }

    @Override
    public T removeItem(int position) {
        return null;
    }

    @Override
    public boolean removeItem(T model) {
        return false;
    }

    @Override
    public void itemsClear() {

    }

    @Override
    public void addAll(List<T> data) {

    }

    @Override
    public void addAll(int position, List<T> data) {

    }

    @Override
    public void removeSubList(int startPosition, int count) {

    }

    @Override
    public void swap(int fromPosition, int toPosition) {

    }

    @Override
    public T getItem(int position) {
        return null;
    }
}
