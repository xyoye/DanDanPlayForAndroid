package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.BlockManagerPresenter;
import com.xyoye.dandanplay.mvp.view.BlockManagerView;
import com.xyoye.dandanplay.utils.Lifeful;

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
    public List<String> queryBlockData() {
        List<String> blockList = new ArrayList<>();
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(13)
                .query()
                .setColumns(1)
                .execute();
        if (cursor != null){
            while (cursor.moveToNext()){
                blockList.add(cursor.getString(0));
            }
        }
        return blockList;
    }

    @Override
    public void deleteALl() {
        DataBaseManager.getInstance()
                .selectTable(13)
                .delete()
                .postExecute();
    }

    @Override
    public void deleteBlock(List<String> textList) {
        for (String text : textList){
            DataBaseManager.getInstance()
                    .selectTable(13)
                    .delete()
                    .where(1, text)
                    .execute();
        }

    }

    @Override
    public void addBlock(List<String> textList) {
        for (String text : textList){
            DataBaseManager.getInstance()
                    .selectTable(13)
                    .insert()
                    .param(1, text)
                    .execute();
        }
    }
}
