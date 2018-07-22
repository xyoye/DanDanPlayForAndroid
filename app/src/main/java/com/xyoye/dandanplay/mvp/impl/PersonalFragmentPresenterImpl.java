package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragmentPresenterImpl extends BaseMvpPresenter<PersonalFragmentView> implements PersonalFragmentPresenter {

    public PersonalFragmentPresenterImpl(PersonalFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        System.out.println("process");
    }

    @Override
    public void resume() {
        getView().changeView();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }


}
