package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;

/**
 * Created by xyoye on 2018/6/29 0029.
 */

public class PersonalFragmentPresenterImpl extends BaseMvpPresenterImpl<PersonalFragmentView> implements PersonalFragmentPresenter {


    public PersonalFragmentPresenterImpl(PersonalFragmentView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
