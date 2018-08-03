package com.xyoye.core.adapter;

import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xyoye.core.R;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.interf.FootLoading;
import com.xyoye.core.utils.PixelUtil;


/**
 * Created by yzd on 2016/1/3.
 */
public class LoadMoreItem implements AdapterItem, FootLoading {

    private View itemView;
    private TextView tv;
    private ProgressBar bar;
    private int gloaH;
    private
    @ColorInt
    int color = 0x00000000;

    private boolean isCanLoading = true;
    private boolean isLoading = false;

    public void setBackgroudColor(@ColorInt int color) {
        this.color = color;
    }

    public void setCanLoading(boolean canLoading) {
        isCanLoading = canLoading;
    }

    public boolean isCanLoading() {
        return isCanLoading;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.loading_more;
    }

    @Override
    public void initItemViews(View itemView) {
        gloaH = PixelUtil.getScreenH();
        this.itemView = itemView;
        tv = itemView.findViewById(R.id.loading_tv);
        bar = itemView.findViewById(R.id.loading_progress);
        setIndexLoadMoreState(isCanLoading);
    }

    @Override
    public void onSetViews() {
        tv.setVisibility(View.VISIBLE);
        itemView.setBackgroundColor(color);
    }

    @Override
    public void onUpdateViews(Object model, int position) {

    }

    /**
     * 第一页加载数据完成时调用的方法
     *
     * @param isCanLoadMore
     */
    @Override
    public void setIndexLoadMoreState(boolean isCanLoadMore) {
        this.isLoading = false;
        this.isCanLoading = isCanLoadMore;
        if (itemView != null) {
            if (isCanLoadMore) {
                itemView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = gloaH / 12;
                completeLoading();
            } else {
                itemView.setVisibility(View.GONE);
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = gloaH;
                nothing();
            }
        }
    }

    /**
     * 上拉加载完成调用的方法
     *
     * @param isCanLoadMore
     */
    @Override
    public void setLoadMoreState(boolean isCanLoadMore) {
        this.isLoading = false;
        this.isCanLoading = isCanLoadMore;
        if (itemView != null) {
            if (isCanLoadMore) {
                completeLoading();
            } else {
                noDataToLoad();
            }
        }
    }

    /**
     * 加载数据调用的方法
     */
    @Override
    public void loadingMore() {
        isLoading = true;
        if (itemView != null) {
            tv.setText("加载中...");
            bar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void completeLoading() {
        tv.setText("上拉加载");
        bar.setVisibility(View.GONE);
    }

    @Override
    public void noDataToLoad() {
        tv.setText("--加载完毕--");
        bar.setVisibility(View.GONE);
    }

    @Override
    public void nothing() {
        //tv.setText("--没有数据--");
        bar.setVisibility(View.GONE);
    }

    public boolean isLoading() {
        return isLoading;
    }

}
