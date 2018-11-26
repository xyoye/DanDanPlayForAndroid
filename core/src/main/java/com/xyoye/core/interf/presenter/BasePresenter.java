package com.xyoye.core.interf.presenter;

import android.os.Bundle;

/**
 * Created by xyy on 2017/6/23.
 */
public interface BasePresenter extends Presenter {

    void init();

    void initPage();

    void process(Bundle savedInstanceState);
}
