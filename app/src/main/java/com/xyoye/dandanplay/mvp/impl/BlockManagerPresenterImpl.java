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
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .query()
                .queryColumns("text")
                .postExecute(new QueryAsyncResultCallback<List<String>>(getLifeful()) {
                    @Override
                    public List<String> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<String> blocks = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            blocks.add(cursor.getString(0));
                        }
                        return blocks;
                    }

                    @Override
                    public void onResult(List<String> result) {
                        getView().updateData(result);
                    }
                });
    }

    @Override
    public void deleteALl() {
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .delete()
                .postExecute();
        DanmuFilterUtils.getInstance().updateLocalFilter();
    }

    @Override
    public void deleteBlock(List<String> textList) {
        for (String text : textList) {
            DataBaseManager.getInstance()
                    .selectTable("danmu_block")
                    .delete()
                    .where("text", text)
                    .postExecute();
        }
        DanmuFilterUtils.getInstance().updateLocalFilter();

    }

    @Override
    public void addBlock(List<String> textList) {
        for (String text : textList) {
            DataBaseManager.getInstance()
                    .selectTable("danmu_block")
                    .insert()
                    .param("text", text)
                    .postExecute();
        }
        DanmuFilterUtils.getInstance().updateLocalFilter();
    }
}
