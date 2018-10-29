package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by YE on 2018/6/28 0028.
 */


public class MainPresenterImpl extends BaseMvpPresenter<MainView> implements MainPresenter {

    public MainPresenterImpl(MainView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        initAnimeType();
        initSubGroup();
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

    private void initAnimeType(){
        AnimeTypeBean.getAnimeType(new CommOtherDataObserver<AnimeTypeBean>(getLifeful()) {
            @Override
            public void onSuccess(AnimeTypeBean animeTypeBean) {
                if (animeTypeBean != null && animeTypeBean.getTypes() != null && animeTypeBean.getTypes().size() > 0){
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[4], "", new String[]{});

                    ContentValues firstValues = new ContentValues();
                    firstValues.put(DataBaseInfo.getFieldNames()[4][1], -1);
                    firstValues.put(DataBaseInfo.getFieldNames()[4][2], "全部");
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[4], null, firstValues);

                    for (AnimeTypeBean.TypesBean typesBean : animeTypeBean.getTypes()){
                        ContentValues values = new ContentValues();
                        values.put(DataBaseInfo.getFieldNames()[4][1],typesBean.getId());
                        values.put(DataBaseInfo.getFieldNames()[4][2],typesBean.getName());
                        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[4], null, values);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    private void initSubGroup(){
        SubGroupBean.getSubGroup(new CommOtherDataObserver<SubGroupBean>(getLifeful()) {
            @Override
            public void onSuccess(SubGroupBean subGroupBean) {
                if (subGroupBean != null && subGroupBean.getSubgroups() != null && subGroupBean.getSubgroups().size() > 0){
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[5], "", new String[]{});

                    //全部
                    ContentValues firstValues = new ContentValues();
                    firstValues.put(DataBaseInfo.getFieldNames()[5][1], -1);
                    firstValues.put(DataBaseInfo.getFieldNames()[5][2], "全部");
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[5], null, firstValues);

                    for (SubGroupBean.SubgroupsBean subgroupsBean : subGroupBean.getSubgroups()){
                        ContentValues values = new ContentValues();
                        values.put(DataBaseInfo.getFieldNames()[5][1],subgroupsBean.getId());
                        values.put(DataBaseInfo.getFieldNames()[5][2],subgroupsBean.getName());
                        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[5], null, values);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
