package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
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
import com.xyoye.dandanplay.mvp.presenter.SearchPresenter;
import com.xyoye.dandanplay.mvp.view.SearchView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.util.ArrayList;
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
        DataBaseManager.getInstance()
                .selectTable("search_history")
                .query()
                .setOrderByColumnDesc("time")
                .postExecute(new QueryAsyncResultCallback<List<SearchHistoryBean>>(getLifeful()) {
                    @Override
                    public List<SearchHistoryBean> onQuery(Cursor cursor) {
                        if (cursor == null) return new ArrayList<>();
                        List<SearchHistoryBean> historyBeanList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            int _id = cursor.getInt(0);
                            String text = cursor.getString(1);
                            long time = cursor.getLong(2);
                            historyBeanList.add(new SearchHistoryBean(_id, text, time));
                        }
                        //添加清除所有搜索记录，id = -1、text = ""作为标志
                        if (historyBeanList.size() > 0)
                            historyBeanList.add(new SearchHistoryBean(-1, "", -1));

                        return historyBeanList;
                    }

                    @Override
                    public void onResult(List<SearchHistoryBean> result) {
                        getView().refreshHistory(result, doSearch);
                    }
                });
    }

    @Override
    public void queryTypeList() {
        DataBaseManager.getInstance()
                .selectTable("anime_type")
                .query()
                .postExecute(new QueryAsyncResultCallback<List<AnimeTypeBean.TypesBean>>(getLifeful()) {

                    @Override
                    public List<AnimeTypeBean.TypesBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<AnimeTypeBean.TypesBean> typeList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            int typeId = cursor.getInt(1);
                            String typeName = cursor.getString(2);
                            typeList.add(new AnimeTypeBean.TypesBean(typeId, typeName));
                        }
                        return typeList;
                    }

                    @Override
                    public void onResult(List<AnimeTypeBean.TypesBean> result) {
                        getView().showAnimeTypeDialog(result);
                    }
                });

    }

    @Override
    public void querySubGroupList() {
        DataBaseManager.getInstance()
                .selectTable("subgroup")
                .query()
                .postExecute(new QueryAsyncResultCallback<List<SubGroupBean.SubgroupsBean>>(getLifeful()) {
                    @Override
                    public List<SubGroupBean.SubgroupsBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<SubGroupBean.SubgroupsBean> subgroupList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            int subgroupId = cursor.getInt(1);
                            String subgroupName = cursor.getString(2);
                            subgroupList.add(new SubGroupBean.SubgroupsBean(subgroupId, subgroupName));
                        }
                        return subgroupList;
                    }

                    @Override
                    public void onResult(List<SubGroupBean.SubgroupsBean> result) {
                        getView().showSubGroupDialog(result);
                    }
                });

    }

    @Override
    public void addHistory(String text) {
        DataBaseManager.getInstance()
                .selectTable("search_history")
                .insert()
                .param("text", text)
                .param("time", System.currentTimeMillis())
                .postExecute();
    }

    @Override
    public void updateHistory(int _id) {
        DataBaseManager.getInstance()
                .selectTable("search_history")
                .update()
                .param("time", System.currentTimeMillis())
                .where("_id", _id + "")
                .postExecute();
    }

    @Override
    public void deleteHistory(int _id) {
        DataBaseManager.getInstance()
                .selectTable("search_history")
                .delete()
                .where("_id", _id + "")
                .postExecute();
    }

    @Override
    public void deleteAllHistory() {
        DataBaseManager.getInstance()
                .selectTable("search_history")
                .delete()
                .postExecute();
    }

    @Override
    public void search(String text, int type, int subgroup) {
        MagnetBean.searchMagnet(text, type, subgroup, new CommOtherDataObserver<MagnetBean>(getLifeful()) {
            @Override
            public void onSuccess(MagnetBean magnetBean) {
                if (magnetBean != null && magnetBean.getResources() != null) {
                    getView().refreshSearch(magnetBean.getResources());
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void searchLocalTorrent(String magnet) {
        String torrentPath = isTorrentExist(magnet);
        if (!StringUtils.isEmpty(torrentPath)) {
            getView().downloadExisted(torrentPath, magnet);
        } else {
            downloadTorrent(magnet);
        }
    }

    @Override
    public void downloadTorrent(String magnet) {
        getView().showDownloadTorrentLoading();
        MagnetBean.downloadTorrent(magnet, new CommOtherDataObserver<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                String downloadPath = getView().getDownloadFolder();
                downloadPath += Constants.DefaultConfig.torrentFolder;
                downloadPath += "/" + magnet.substring(20) + ".torrent";
                FileIOUtils.writeFileFromIS(downloadPath, responseBody.byteStream());
                getView().dismissDownloadTorrentLoading();
                getView().downloadTorrentOver(downloadPath, magnet);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().dismissDownloadTorrentLoading();
                LogUtils.e(message);
                ToastUtils.showShort("下载种子文件失败");
            }
        }, new NetworkConsumer());
    }

    //判断该种子是否已存在
    private String isTorrentExist(String magnet) {
        String downloadPath = getView().getDownloadFolder();
        downloadPath += Constants.DefaultConfig.torrentFolder;
        downloadPath += "/" + magnet.substring(20) + ".torrent";
        File file = new File(downloadPath);
        if (file.exists()) {
            return downloadPath;
        } else {
            downloadPath = AppConfig.getInstance().getDownloadFolder();
            downloadPath += Constants.DefaultConfig.torrentFolder;
            downloadPath += "/" + magnet.substring(20) + ".torrent";
            if (file.exists())
                return downloadPath;
            else
                return "";
        }
    }
}
