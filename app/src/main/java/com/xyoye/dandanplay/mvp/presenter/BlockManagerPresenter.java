package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyoye on 2019/6/26.
 */

public interface BlockManagerPresenter extends BaseMvpPresenter {

    void queryBlockData();

    void deleteALl();

    void deleteBlock(List<String> text);

    void addBlock(List<String> texts);
}
