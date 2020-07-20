package com.xyoye.dandanplay.ui.weight.swipe_menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.xyoye.dandanplay.R;

import java.util.ArrayList;

import static com.xyoye.dandanplay.ui.weight.swipe_menu.SwipeState.SWIPE_CLOSE;

/**
 * Created by guanaj on 2017/6/5.
 * <p>
 * Modified by xyoye on 2020/6/24.
 */

public class EasySwipeMenuLayout extends ViewGroup {

    private ArrayList<View> mMatchParentChildren = new ArrayList<>(1);
    private static EasySwipeMenuLayout mViewCache;

    private final int mContentViewResID;
    private final int mTopViewResID;
    private final int mBottomViewResID;
    private final int mLeftViewResID;
    private final int mRightViewResID;

    private View mContentView;
    private View mTopView;
    private View mBottomView;
    private View mLeftView;
    private View mRightView;

    private PointF mLastP;
    private PointF mFirstP;
    private Scroller mScroller;
    private MarginLayoutParams mContentViewLp;

    private final int mScaledTouchSlop;

    private SwipeState mSwipeState;
    private boolean isSwiping;
    private float finallyDistanceX;

    private boolean mLeftSwipeEnable = true;
    private boolean mRightSwipeEnable = true;
    private boolean allowHorizontalSwipe = true;

    public EasySwipeMenuLayout(Context context) {
        this(context, null);
    }

    public EasySwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasySwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasySwipeMenuLayout, defStyleAttr, 0);
        mContentViewResID = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_contentView, -1);
        mTopViewResID = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_topMenuView, -1);
        mBottomViewResID = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_bottomMenuView, -1);
        mLeftViewResID = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_leftMenuView, -1);
        mRightViewResID = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_rightMenuView, -1);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setClickable(true);
        int count = getChildCount();
        //参考frameLayout测量代码
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int left = getPaddingLeft();
        int right = getPaddingLeft();
        int top = getPaddingTop();
        int bottom = getPaddingTop();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (mLeftView == null && child.getId() == mLeftViewResID) {
                mLeftView = child;
                mLeftView.setClickable(true);
            } else if (mRightView == null && child.getId() == mRightViewResID) {
                mRightView = child;
                mRightView.setClickable(true);
            } else if (mContentView == null && child.getId() == mContentViewResID) {
                mContentView = child;
                mContentView.setClickable(true);
            } else if (mBottomView == null && child.getId() == mBottomViewResID) {
                mBottomView = child;
                mBottomView.setClickable(true);
            } else if (mTopView == null && child.getId() == mTopViewResID) {
                mTopView = child;
                mTopView.setClickable(true);
            }
        }
        if (mContentView != null) {
            mContentViewLp = (MarginLayoutParams) mContentView.getLayoutParams();
            int cTop = top + mContentViewLp.topMargin;
            int cLeft = left + mContentViewLp.leftMargin;
            int cRight = left + mContentViewLp.leftMargin + mContentView.getMeasuredWidth();
            int cBottom = cTop + mContentView.getMeasuredHeight();
            mContentView.layout(cLeft, cTop, cRight, cBottom);
        }
        if (mLeftView != null) {
            MarginLayoutParams leftViewLp = (MarginLayoutParams) mLeftView.getLayoutParams();
            int lTop = top + leftViewLp.topMargin;
            int lLeft = 0 - mLeftView.getMeasuredWidth() + leftViewLp.leftMargin + leftViewLp.rightMargin;
            int lRight = 0 - leftViewLp.rightMargin;
            int lBottom = lTop + mLeftView.getMeasuredHeight();
            mLeftView.layout(lLeft, lTop, lRight, lBottom);
        }
        if (mRightView != null) {
            MarginLayoutParams rightViewLp = (MarginLayoutParams) mRightView.getLayoutParams();
            int lTop = top + rightViewLp.topMargin;
            int lLeft = mContentView.getRight() + mContentViewLp.rightMargin + rightViewLp.leftMargin;
            int lRight = lLeft + mRightView.getMeasuredWidth();
            int lBottom = lTop + mRightView.getMeasuredHeight();
            mRightView.layout(lLeft, lTop, lRight, lBottom);
        }
        if (mBottomView != null) {
            MarginLayoutParams bottomViewLp = (MarginLayoutParams) mBottomView.getLayoutParams();
            int lTop = mContentView.getBottom() + mContentViewLp.topMargin + bottomViewLp.bottomMargin;
            int lLeft = left + bottomViewLp.leftMargin;
            int lRight = left + bottomViewLp.leftMargin + mBottomView.getMeasuredWidth();
            int lBottom = lTop + mBottomView.getMeasuredHeight();
            mBottomView.layout(lLeft, lTop, lRight, lBottom);
        }
        if (mTopView != null) {
            MarginLayoutParams topViewLp = (MarginLayoutParams) mTopView.getLayoutParams();
            int lTop = 0 - mTopView.getMeasuredHeight() + topViewLp.topMargin + topViewLp.bottomMargin;
            int lLeft = left + topViewLp.leftMargin;
            int lRight = lLeft + mTopView.getMeasuredWidth();
            int lBottom = 0 - topViewLp.bottomMargin;
            mTopView.layout(lLeft, lTop, lRight, lBottom);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                isSwiping = false;

                allowHorizontalSwipe = mSwipeState == null ||
                        mSwipeState == SWIPE_CLOSE ||
                        mSwipeState == SwipeState.SWIPE_LEFT ||
                        mSwipeState == SwipeState.SWIPE_RIGHT;

                if (mLastP == null) {
                    mLastP = new PointF();
                }
                mLastP.set(ev.getRawX(), ev.getRawY());
                if (mFirstP == null) {
                    mFirstP = new PointF();
                }
                mFirstP.set(ev.getRawX(), ev.getRawY());
                if (mViewCache != null) {
                    if (mViewCache != this) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        mViewCache.handlerSwipeMenu(SWIPE_CLOSE);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float distanceX = mLastP.x - ev.getRawX();
                float distanceY = mLastP.y - ev.getRawY();

                if (!allowHorizontalSwipe) {
                    break;
                }

                if (Math.abs(distanceY) > Math.abs(distanceX)){
                    getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }

                scrollBy((int) (distanceX), 0);
                if (getScrollX() < 0) {
                    if (!mRightSwipeEnable || mLeftView == null) {
                        scrollTo(0, 0);
                    } else {
                        if (getScrollX() < mLeftView.getLeft()) {
                            scrollTo(mLeftView.getLeft(), 0);
                        }
                    }
                } else if (getScrollX() > 0) {
                    if (!mLeftSwipeEnable || mRightView == null) {
                        scrollTo(0, 0);
                    } else {
                        if (getScrollX() > mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin) {
                            scrollTo(mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin, 0);
                        }
                    }
                }

                getParent().requestDisallowInterceptTouchEvent(true);
                mLastP.set(ev.getRawX(), ev.getRawY());
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                finallyDistanceX = mFirstP.x - ev.getRawX();
                if (Math.abs(finallyDistanceX) > mScaledTouchSlop) {
                    isSwiping = true;
                }
                handlerSwipeMenu(isShouldOpen());
                allowHorizontalSwipe = true;
                break;
            }
            default: {
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(finallyDistanceX) > mScaledTouchSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isSwiping) {
                    isSwiping = false;
                    finallyDistanceX = 0;
                    return true;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 手动设置状态
     */
    private void handlerSwipeMenu(SwipeState result) {
        if (result == SwipeState.SWIPE_LEFT) {
            mScroller.startScroll(getScrollX(), 0, mLeftView.getLeft() - getScrollX(), 0);
            mViewCache = this;
            mSwipeState = result;
        } else if (result == SwipeState.SWIPE_RIGHT) {
            mScroller.startScroll(getScrollX(), 0, mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin - getScrollX(), 0);
            mViewCache = this;
            mSwipeState = result;
        } else if (result == SwipeState.SWIPE_BOTTOM) {
            mScroller.startScroll(0, getScrollY(), 0, mBottomView.getBottom() - mContentView.getBottom() - mContentViewLp.bottomMargin - getScrollY());
            mViewCache = this;
            mSwipeState = result;
        } else if (result == SwipeState.SWIPE_TOP) {
            mScroller.startScroll(0, getScrollY(), 0, mTopView.getTop() - getScrollY());
            mViewCache = this;
            mSwipeState = result;
        } else {
            closeMenu();
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 根据当前的scrollX的值判断松开手后应处于何种状态
     */
    private SwipeState isShouldOpen() {
        int scrollX = getScrollX();
        if (Math.abs(finallyDistanceX) < mScaledTouchSlop) {
            return mSwipeState;
        }
        if (finallyDistanceX < 0) {
            if (scrollX < 0 && mLeftView != null) {
                if (Math.abs(scrollX) > mScaledTouchSlop) {
                    return SwipeState.SWIPE_LEFT;
                }
            }
            if (scrollX > 0 && mRightView != null) {
                return SWIPE_CLOSE;
            }
        } else if (finallyDistanceX > 0) {
            if (scrollX > 0 && mRightView != null) {
                if (Math.abs(scrollX) > mScaledTouchSlop) {
                    return SwipeState.SWIPE_RIGHT;
                }
            }
            if (scrollX < 0 && mLeftView != null) {
                return SWIPE_CLOSE;
            }
        }
        return SWIPE_CLOSE;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.handlerSwipeMenu(SWIPE_CLOSE);
        }
        super.onDetachedFromWindow();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this == mViewCache) {
            mViewCache.handlerSwipeMenu(mSwipeState);
        }
    }

    public void closeMenu() {
        if (mViewCache != null) {
            if (mSwipeState != null && mSwipeState != SWIPE_CLOSE && mScroller != null) {
                if (mSwipeState == SwipeState.SWIPE_RIGHT || mSwipeState == SwipeState.SWIPE_LEFT) {
                    mScroller.startScroll(mViewCache.getScrollX(), 0, -mViewCache.getScrollX(), 0);
                } else {
                    mScroller.startScroll(0, mViewCache.getScrollY(), 0, -mViewCache.getScrollY());
                }
                mViewCache.invalidate();
                mViewCache = null;
                mSwipeState = null;
            }
        }
        ViewParent parent = getParent();
        if (parent != null){
            parent.requestDisallowInterceptTouchEvent(false);
        }
    }

    public void openMenu(@NonNull SwipeState swipeState) {
        if (swipeState == mSwipeState) {
            return;
        }
        if (mSwipeState != null) {
            closeMenu();
        }
        if (mScroller != null) {
            handlerSwipeMenu(swipeState);
        }
    }

    public boolean isLeftSwipeEnable() {
        return mLeftSwipeEnable;
    }

    public void setLeftSwipeEnable(boolean leftSwipeEnable) {
        this.mLeftSwipeEnable = leftSwipeEnable;
    }

    public boolean isRightSwipeEnable() {
        return mRightSwipeEnable;
    }

    public void setRightSwipeEnable(boolean rightSwipeEnable) {
        this.mRightSwipeEnable = rightSwipeEnable;
    }

    public SwipeState getSwipeState() {
        return mSwipeState;
    }
}
