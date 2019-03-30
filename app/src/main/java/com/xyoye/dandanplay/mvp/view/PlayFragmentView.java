package com.xyoye.dandanplay.mvp.view;

import android.content.Context;

import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PlayFragmentView extends BaseMvpView, LoadDataView {
    void refreshAdapter(List<FolderBean> beans);

    void refreshOver();

    Context getContext();
}
