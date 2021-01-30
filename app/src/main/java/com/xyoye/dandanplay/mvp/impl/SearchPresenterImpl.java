package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.mvp.presenter.SearchPresenter;
import com.xyoye.dandanplay.mvp.view.SearchView;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by xyoye on 2019/1/8.
 */

public class SearchPresenterImpl extends BaseMvpPresenterImpl<SearchView> implements SearchPresenter {

    public SearchPresenterImpl(SearchView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
                .postExecute(new QueryAsyncResultCallback<List<SearchHistoryBean>>(getLifecycle()) {
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
        getView().showLoading();
        AnimeTypeBean.getAnimeType(new CommOtherDataObserver<AnimeTypeBean>(getLifecycle()) {
            @Override
            public void onSuccess(AnimeTypeBean animeTypeBean) {
                getView().hideLoading();
                if (animeTypeBean != null
                        && animeTypeBean.getTypes() != null
                        && animeTypeBean.getTypes().size() > 0) {
                    getView().showAnimeTypeDialog(animeTypeBean.getTypes());
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
    public void querySubGroupList() {
        getView().showLoading();
        SubGroupBean.getSubGroup(new CommOtherDataObserver<SubGroupBean>(getLifecycle()) {
            @Override
            public void onSuccess(SubGroupBean subGroupBean) {
                getView().hideLoading();
                if (subGroupBean != null
                        && subGroupBean.getSubgroups() != null
                        && subGroupBean.getSubgroups().size() > 0) {
                    getView().showSubGroupDialog(subGroupBean.getSubgroups());
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
    public void search(String animeTitle, String text, int type, int subgroup) {
        getView().showLoading();
        MagnetBean.searchMagnet(animeTitle, text, type, subgroup, new CommOtherDataObserver<MagnetBean>(getLifecycle()) {
            @Override
            public void onSuccess(MagnetBean magnetBean) {
                getView().hideLoading();
                if (magnetBean != null && magnetBean.getResources() != null) {
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
    public void downloadTorrent(String magnet, int position, boolean onlyDownload, boolean playResource) {
        getView().showDownloadTorrentLoading();
        MagnetBean.downloadTorrent(magnet, new CommOtherDataObserver<ResponseBody>(getLifecycle()) {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                String downloadPath = getView().getDownloadFolder();
                downloadPath += Constants.DefaultConfig.torrentFolder;
                int indexOfLast = magnet.indexOf("&dn");
                if (indexOfLast < 0) {
                    indexOfLast = magnet.length();
                }
                downloadPath += "/" + magnet.substring(20, indexOfLast) + ".torrent";
                FileIOUtils.writeFileFromIS(downloadPath, responseBody.byteStream());
                getView().dismissDownloadTorrentLoading();
                getView().downloadTorrentOver(downloadPath, position, onlyDownload, playResource);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().dismissDownloadTorrentLoading();
                LogUtils.e(message);
                ToastUtils.showShort("下载种子文件失败");
            }
        }, new NetworkConsumer());
    }
}
