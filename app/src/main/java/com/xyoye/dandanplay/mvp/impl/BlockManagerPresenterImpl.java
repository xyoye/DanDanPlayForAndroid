package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.BlockManagerPresenter;
import com.xyoye.dandanplay.mvp.view.BlockManagerView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

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

        return DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .query()
                .queryColumns("text")
                .execute(cursor -> {
                    List<String> blocks = new ArrayList<>();
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            blocks.add(cursor.getString(0));
                        }
                    }
                    return blocks;
                });
    }

    @Override
    public void deleteALl() {
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .delete()
                .postExecute();
    }

    @Override
    public void deleteBlock(List<String> textList) {
        for (String text : textList) {
            DataBaseManager.getInstance()
                    .selectTable("danmu_block")
                    .delete()
                    .where("text", text)
                    .execute();
        }

    }

    @Override
    public void addBlock(List<String> textList) {
        for (String text : textList) {
            DataBaseManager.getInstance()
                    .selectTable("danmu_block")
                    .insert()
                    .param("text", text)
                    .execute();
        }
    }
}
