package com.xyoye.core.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.xyoye.core.R;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by xyy on 2017/6/23.
 *
 * 如果调用{@link #notifyDataSetChanged()}来更新，
 * 它会自动调用{@link #instantiateItem(ViewGroup, int)}重新new出需要的item，算是完全初始化一次。
 */
public abstract class BasePagerAdapter<T> extends PagerAdapter {

    protected T currentItem = null;

    /**
     * 这的cache的最大大小是：type * pageSize
     */
    private final PagerCache<T> mCache;

    public BasePagerAdapter() {
        mCache = new PagerCache<>();
    }

    /**
     * 注意：这里必须是view和view的比较
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == getViewFromItem((T) obj, 0);
    }

    @Override
    public T instantiateItem(ViewGroup container, int position) {
        Object type = getItemType(position);
        T item = mCache.getItem(type); // get item from type
        if (item == null) {
            item = createItem(container, position);
        }
        // 通过item得到将要被add到viewpager中的view
        View view = getViewFromItem(item, position);
        view.setTag(R.id.tag, type);

        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        container.addView(view);
        return item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (object != currentItem) {
            // 可能是currentItem不等于null，可能是二者不同
            currentItem = (T) object;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        T item = (T) object;
        // 现在通过item拿到其中的view，然后remove掉
        container.removeView(getViewFromItem(item, position));
        Object type = getViewFromItem(item, position).getTag(R.id.tag);
        mCache.putItem(type, item);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    protected int getItemType(int position) {
        return -1; // default
    }

    public T getCurrentItem() {
        return currentItem;
    }

    protected PagerCache<T> getCache() {
        return mCache;
    }

    /**
     * 这里要实现一个从item拿到view的规则
     *
     * @param item     包含view的item对象
     * @param position item所处的位置
     * @return item中的view对象
     */
    protected abstract
    @NonNull
    View getViewFromItem(T item, int position);

    /**
     * 当缓存中无法得到所需item时才会调用
     *
     * @return 需要放入容器的view
     */
    protected abstract T createItem(ViewGroup viewPager, int position);

    ///////////////////////////////////////////////////////////////////////////
    // 缓存类
    ///////////////////////////////////////////////////////////////////////////

    private static class PagerCache<T> {

        private Map<Object, Queue<T>> mCacheMap;

        public PagerCache() {
            mCacheMap = new ArrayMap<>();
        }

        /**
         * @param type item type
         * @return cache中的item，如果拿不到就返回null
         */
        public T getItem(Object type) {
            Queue<T> queue = mCacheMap.get(type);
            return queue != null ? queue.poll() : null;
        }

        /**
         * @param type item's type
         */
        public void putItem(Object type, T item) {
            Queue<T> queue;
            if ((queue = mCacheMap.get(type)) == null) {
                queue = new LinkedList<>();
                mCacheMap.put(type, queue);
            }
            queue.offer(item);
        }
    }
}
