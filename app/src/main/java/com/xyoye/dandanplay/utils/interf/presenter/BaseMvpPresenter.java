package com.xyoye.dandanplay.utils.interf.presenter;

import android.os.Bundle;

/**
 * Modified by xyoye on 2017/6/23.
 */
public interface BaseMvpPresenter extends Presenter {

    void init();

    void initPage();

    void process(Bundle savedInstanceState);
}
