package com.xyoye.dandanplay.mvp.impl;

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
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.SearchMagnetPresenter;
import com.xyoye.dandanplay.mvp.view.SearchMagnetView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by YE on 2018/10/13.
 */


public class SearchMagnetPresenterImpl extends BaseMvpPresenterImpl<SearchMagnetView> implements SearchMagnetPresenter {
    private String savePath;

    public SearchMagnetPresenterImpl(SearchMagnetView view, Lifeful lifeful) {
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
    public void searchMagnet(String anime, int typeId, int subGroundId) {
        getView().showLoading();
        MagnetBean.searchMagnet(anime, typeId, subGroundId, new CommOtherDataObserver<MagnetBean>(getLifeful()) {
            @Override
            public void onSuccess(MagnetBean magnetBean) {
                if (magnetBean != null && magnetBean.getResources() != null){
                    getView().hideLoading();
                    for (MagnetBean.ResourcesBean bean : magnetBean.getResources()){
                        bean.setEpisodeId(getView().getEpisodeId());
                    }
                    getView().refreshAdapter(magnetBean.getResources());
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
    public void downloadTorrent(String animeTitle, String magnet) {
        this.savePath = AppConfig.getInstance().getDownloadFolder() + animeTitle;

        //判断是否已经下载过该种子
        String donePath = isDoneTorrent(savePath , magnet);
        if (!StringUtils.isEmpty(donePath)){
            getView().downloadTorrentOver(donePath, magnet);
            return;
        }

        getView().showLoading("下载准备中");
        MagnetBean.downloadTorrent(magnet, new CommOtherDataObserver<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                SearchMagnetPresenterImpl.this.savePath += "/torrent/";
                SearchMagnetPresenterImpl.this.savePath += magnet.substring(20, magnet.length()) +".torrent";
                FileIOUtils.writeFileFromIS(SearchMagnetPresenterImpl.this.savePath, responseBody.byteStream());
                getView().hideLoading();
                getView().downloadTorrentOver(SearchMagnetPresenterImpl.this.savePath, magnet);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                LogUtils.e(message);
                ToastUtils.showShort("下载种子文件失败");
            }
        }, new NetworkConsumer());
    }

    private String isDoneTorrent(String savePath, String magnet){
        String path = savePath + "/torrent/" + magnet.substring(20, magnet.length()) +".torrent";
        File file = new File(path);
        if (file.exists())
            return path;
        else
            return "";
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
}
