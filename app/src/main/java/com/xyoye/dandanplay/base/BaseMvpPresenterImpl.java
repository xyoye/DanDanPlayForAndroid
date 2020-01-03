package com.xyoye.dandanplay.base;

import com.xyoye.dandanplay.utils.Lifeful;
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
    private Lifeful lifeful;
    protected List<Disposable> disposables;

    public BaseMvpPresenterImpl(T view, Lifeful lifeful) {
        this.view = view;
        this.lifeful = lifeful;
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

    public Lifeful getLifeful() {
        return lifeful;
    }
}
