/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2015 cpoopc
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.xyoye.dandanplay.ui.weight;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ScrollView;

import com.blankj.utilcode.util.LogUtils;

/**
 * Modified by xyoye on 2018/5/22.
 */

public class ScrollableHelper {

    private ScrollableContainer mCurrentScrollableCainer;

    /**
     * a viewgroup whitch contains ScrollView/ListView/RecycelerView..
     */
    public interface ScrollableContainer {
        /**
         * @return ScrollView/ListView/RecycelerView..'s instance
         */
        View getScrollableView();
    }

    public void setCurrentScrollableContainer(ScrollableContainer scrollableContainer) {
        this.mCurrentScrollableCainer = scrollableContainer;
    }

    public View getScrollableView() {
        if (mCurrentScrollableCainer == null) {
            return null;
        }
        return mCurrentScrollableCainer.getScrollableView();
    }

    /**
     * 判断是否滑动到顶部方法,ScrollAbleLayout根据此方法来做一些逻辑判断
     * 目前只实现了AdapterView,ScrollView,RecyclerView
     * 需要支持其他view可以自行补充实现
     *
     * @return
     */
    public boolean isTop() {
        View scrollableView = getScrollableView();
        if (scrollableView == null) {
//            throw new NullPointerException("You should call ScrollableHelper.setCurrentScrollableContainer() to set ScrollableContainer.");
            return true;
        }
        if (scrollableView instanceof AdapterView) {
            return isAdapterViewTop((AdapterView) scrollableView);
        }
        if (scrollableView instanceof ScrollView) {
            return isScrollViewTop((ScrollView) scrollableView);
        }
        if (scrollableView instanceof RecyclerView) {
            boolean top = isRecyclerViewTop((RecyclerView) scrollableView);
            LogUtils.d("isTop", top + "");
            return top;
        }
        if (scrollableView instanceof NestedScrollView) {
            boolean top = isNestedScrollViewTop((NestedScrollView) scrollableView);
            LogUtils.d("isTop", top + "");
            return top;
        }
        if (scrollableView instanceof WebView) {
            return isWebViewTop((WebView) scrollableView);
        }
        if (scrollableView instanceof CardView)
        {
            return isCardViewTop((CardView) scrollableView);
        }
        throw new IllegalStateException("scrollableView must be a instance of AdapterView|ScrollView|RecyclerView|CardView");
    }

    private static boolean isRecyclerViewTop(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                // int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                View childAt = recyclerView.getChildAt(0);
                /*if (childAt == null || (firstVisibleItemPosition == 0 && childAt.getTop() == 0)) {
                    return true;
                }*/
                int isFirst = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                return childAt == null || isFirst == 0;
            }
        }
        return false;
    }

    private static boolean isAdapterViewTop(AdapterView adapterView) {
        if (adapterView != null) {
            int firstVisiblePosition = adapterView.getFirstVisiblePosition();
            View childAt = adapterView.getChildAt(0);
            return childAt == null || (firstVisiblePosition == 0 && childAt.getTop() == 0);
        }
        return false;
    }

    private static boolean isNestedScrollViewTop(NestedScrollView nestedScrollView) {
        if (nestedScrollView != null) {
            int scrollViewY = nestedScrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    private static boolean isScrollViewTop(ScrollView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    private static boolean isWebViewTop(WebView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    private static boolean isCardViewTop(CardView cardView) {
        //only support CardView has an ScrollView or WebView child
        if (cardView != null) {
            View firstChild = cardView.getChildAt(0);
            if (firstChild != null) {
                if (firstChild instanceof ScrollView) {
                    return isScrollViewTop((ScrollView) firstChild);
                }

                if (firstChild instanceof WebView) {
                    return isWebViewTop((WebView) firstChild);
                }
                throw new IllegalStateException("If scrollableView is a instance of CardView, it must have an ScrollView or WebView child.");
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    public void smoothScrollBy(int velocityY, int distance, int duration) {
        View scrollableView = getScrollableView();
        if (scrollableView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) scrollableView;
            if (Build.VERSION.SDK_INT >= 21) {
                absListView.fling(velocityY);
            } else {
                absListView.smoothScrollBy(distance, duration);
            }
        } else if (scrollableView instanceof ScrollView) {
            ((ScrollView) scrollableView).fling(velocityY);
        }  else if (scrollableView instanceof NestedScrollView) {
            ((NestedScrollView) scrollableView).fling(velocityY);
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).fling(0, velocityY);
        } else if (scrollableView instanceof WebView) {
            ((WebView) scrollableView).flingScroll(0, velocityY);
        }
    }

}
