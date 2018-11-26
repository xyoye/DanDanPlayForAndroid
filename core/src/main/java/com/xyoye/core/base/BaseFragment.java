package com.xyoye.core.base;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by xyy on 2017/6/23.
 */

public abstract class BaseFragment<T extends BasePresenter> extends BaseMvpFragment<T> {

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
