package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.SearchPresenter;
import com.xyoye.dandanplay.mvp.view.SearchView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by xyoye on 2019/1/8.
 */

public class SearchPresenterImpl extends BaseMvpPresenterImpl<SearchView> implements SearchPresenter {

    public SearchPresenterImpl(SearchView view, Lifeful lifeful) {
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
    public void getSearchHistory(boolean doSearch) {
        List<SearchHistoryBean> historyBeanList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM search_history ORDER BY time DESC", new String[]{});
        while (cursor.moveToNext()){
            int _id = cursor.getInt(0);
            String text = cursor.getString(1);
            long time = cursor.getLong(2);
            historyBeanList.add(new SearchHistoryBean(_id, text, time));
        }
        cursor.close();
        //按搜索时间排序
        Collections.sort(historyBeanList, (a, b) -> {
            if (a.getTime() == b.getTime()) return 0;
            return a.getTime() < b.getTime() ? 1 : -1 ;
        });
        //添加清除所有搜索记录，id = -1、text = ""作为标志
        if (historyBeanList.size() > 0)
            historyBeanList.add(new SearchHistoryBean(-1, "", -1));
        getView().refreshHistory(historyBeanList, doSearch);
    }

    @Override
    public List<AnimeTypeBean.TypesBean> getTypeList() {
        List<AnimeTypeBean.TypesBean> typeList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM anime_type", new String[]{});
        while (cursor.moveToNext()){
            int typeId = cursor.getInt(1);
            String typeName = cursor.getString(2);
            typeList.add(new AnimeTypeBean.TypesBean(typeId, typeName));
        }
        cursor.close();
        return typeList;
    }

    @Override
    public List<SubGroupBean.SubgroupsBean> getSubGroupList() {
        List<SubGroupBean.SubgroupsBean> subgroupList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM subgroup", new String[]{});
        while (cursor.moveToNext()){
            int subgroupId = cursor.getInt(1);
            String subgroupName = cursor.getString(2);
            subgroupList.add(new SubGroupBean.SubgroupsBean(subgroupId, subgroupName));
        }
        cursor.close();
        return subgroupList;
    }

    @Override
    public void addHistory(String text) {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[9][1], text);
            values.put(DataBaseInfo.getFieldNames()[9][2], System.currentTimeMillis());
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[9], null, values);
        }).start();
    }

    @Override
    public void updateHistory(int _id) {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[9][2], System.currentTimeMillis());
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[9], values, "_id = ?", new String[]{_id+""});
        }).start();

    }

    @Override
    public void deleteHistory(int _id) {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[9], "_id = ?", new String[] {_id+""});
        }).start();
    }

    @Override
    public void deleteAllHistory() {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[9], "", new String[] {});
        }).start();
    }

    @Override
    public void search(String text, int type, int subgroup) {
        getView().showLoading();
        MagnetBean.searchMagnet(text, type, subgroup, new CommOtherDataObserver<MagnetBean>(getLifeful()) {
            @Override
            public void onSuccess(MagnetBean magnetBean) {
                if (magnetBean != null && magnetBean.getResources() != null){
                    getView().hideLoading();
                    getView().refreshSearch(magnetBean.getResources());
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void searchLocalTorrent(String magnet) {
        String torrentPath = isTorrentExist(magnet);
        if (!StringUtils.isEmpty(torrentPath)){
            getView().downloadExisted(torrentPath, magnet);
        }else {
            downloadTorrent(magnet);
        }
    }

    @Override
    public void downloadTorrent(String magnet){
        getView().showLoading();
        MagnetBean.downloadTorrent(magnet, new CommOtherDataObserver<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                String downloadPath = getView().getDownloadFolder();
                downloadPath += Constants.DefaultConfig.torrentFolder;
                downloadPath += "/" + magnet.substring(20) +".torrent";
                FileIOUtils.writeFileFromIS(downloadPath, responseBody.byteStream());
                getView().hideLoading();
                getView().downloadTorrentOver(downloadPath, magnet);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                LogUtils.e(message);
                ToastUtils.showShort("下载种子文件失败");
            }
        }, new NetworkConsumer());
    }

    //判断该种子是否已存在
    private String isTorrentExist(String magnet){
        String downloadPath = getView().getDownloadFolder();
        downloadPath += Constants.DefaultConfig.torrentFolder;
        downloadPath += "/" + magnet.substring(20) +".torrent";
        File file = new File(downloadPath);
        if (file.exists()){
            return downloadPath;
        } else{
            downloadPath = AppConfig.getInstance().getDownloadFolder();
            downloadPath +=  Constants.DefaultConfig.torrentFolder;
            downloadPath += "/" + magnet.substring(20) +".torrent";
            if (file.exists())
                return downloadPath;
            else
                return "";
        }
    }
}
