package com.xyoye.core.interf.presenter;

import android.os.Bundle;

/**
 * Created by yzd on 2016/8/29.
 */
public interface BasePresenter extends Presenter {

    void init();

    void initPage();

    void process(Bundle savedInstanceState);
}
