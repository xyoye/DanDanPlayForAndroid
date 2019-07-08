package com.xyoye.dandanplay.base;

import android.content.Context;

import com.xyoye.dandanplay.app.BaseApplication;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * mvp中presenter的抽象类
 * Modified by xyoye on 2019/5/27.
 */
public abstract class BaseMvpPresenterImpl<T extends BaseMvpView> implements BaseMvpPresenter {

    private Context mContext;
    private T view;
    private Lifeful lifeful;
    protected List<Disposable> disposables;

    public BaseMvpPresenterImpl(T view) {
        this.view = view;
    }

    @Deprecated
    public BaseMvpPresenterImpl(Context mContext, T view) {
        this.mContext = mContext;
        this.view = view;
    }

    public BaseMvpPresenterImpl(T view, Lifeful lifeful) {
        this.view = view;
        this.lifeful = lifeful;
    }

    @Deprecated
    public BaseMvpPresenterImpl(Context mContext, T view, Lifeful lifeful) {
        this.mContext = mContext;
        this.view = view;
        this.lifeful = lifeful;
    }

    @Override
    public void initPage() {
        getView().initView();
        getView().initListener();
    }

    @Override
    public void destroy() {
        for (Disposable disposable : disposables){
            if (disposable != null)
                disposable.dispose();
        }
    }

    @Deprecated
    public Context getContext() {
        return mContext;
    }

    public Context getApplicationContext() {
        return BaseApplication.get_context();
    }

    public T getView() {
        return view;
    }

    public Lifeful getLifeful() {
        return lifeful;
    }
}
