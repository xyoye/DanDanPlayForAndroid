package com.xyoye.dandanplay.base;


import androidx.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * mvp中presenter的抽象类
 * Modified by xyoye on 2019/5/27.
 */
public abstract class BaseMvpPresenterImpl<T extends BaseMvpView> implements BaseMvpPresenter {

    private T view;
    private LifecycleOwner lifecycleOwner;
    protected List<Disposable> disposables;

    public BaseMvpPresenterImpl(T view, LifecycleOwner lifecycleOwner) {
        this.view = view;
        this.lifecycleOwner = lifecycleOwner;
        disposables = new ArrayList<>();
    }

    @Override
    public void initPage() {
        getView().initView();
        getView().initListener();
    }

    @Override
    public void destroy() {
        if (disposables == null){
            disposables = new ArrayList<>();
        }
        for (Disposable disposable : disposables){
            if (disposable != null)
                disposable.dispose();
        }
    }

    public T getView() {
        return view;
    }

    public LifecycleOwner getLifecycle() {
        return lifecycleOwner;
    }
}
