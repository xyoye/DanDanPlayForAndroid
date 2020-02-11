package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.BlockManagerPresenter;
import com.xyoye.dandanplay.mvp.view.BlockManagerView;
import com.xyoye.dandanplay.utils.DanmuFilterUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/6/26.
 */

public class BlockManagerPresenterImpl extends BaseMvpPresenterImpl<BlockManagerView> implements BlockManagerPresenter {

    public BlockManagerPresenterImpl(BlockManagerView view, Lifeful lifeful) {
        super(view, lifeful);
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

    @Override
    public void queryBlockData() {
        getView().updateData(DanmuFilterUtils.getInstance().getLocalFilter());
    }

    @Override
    public void deleteALl() {
        DanmuFilterUtils.getInstance().clearLocalFilter();
    }

    @Override
    public void deleteBlock(List<String> textList) {
        for (String text : textList) {
            DanmuFilterUtils.getInstance().removeLocalFilter(text);
        }

    }

    @Override
    public void addBlock(List<String> textList) {
        for (String text : textList) {
            DanmuFilterUtils.getInstance().addLocalFilter(text);
        }
    }
}
