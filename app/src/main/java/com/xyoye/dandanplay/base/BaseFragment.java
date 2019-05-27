package com.xyoye.dandanplay.base;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Modified by xyoye on 2019/5/27.
 */

public abstract class BaseFragment<T extends BaseMvpPresenter> extends BaseMvpFragment<T> {

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public BaseAppCompatActivity get() {
        return (BaseAppCompatActivity) this.getContext();
    }
}
