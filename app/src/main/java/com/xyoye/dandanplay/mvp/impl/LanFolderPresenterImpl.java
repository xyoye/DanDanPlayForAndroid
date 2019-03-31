package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.SmbBean2;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.LanFolderPresenter;
import com.xyoye.dandanplay.mvp.view.LanFolderView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.JsonUtil;
import com.xyoye.dandanplay.utils.Lifeful;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyy on 2018/11/21.
 */

public class LanFolderPresenterImpl extends BaseMvpPresenterImpl<LanFolderView> implements LanFolderPresenter {

    public LanFolderPresenterImpl(LanFolderView view, Lifeful lifeful) {
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

    @SuppressLint("CheckResult")
    @Override
    public void getFolders() {
        io.reactivex.Observable
                .create((ObservableOnSubscribe<List<FolderBean>>) emitter ->
                    emitter.onNext(getFolderFormDataBase()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(folderBeans ->
                    getView().refreshFolder(folderBeans));
    }

    @Override
    public void searchFolder(){
        String device  = SPUtils.getInstance().getString(Constants.Config.SMB_DEVICE);
        if (StringUtils.isEmpty(device)){
            ToastUtils.showShort("请先选择共享设备");
            getView().refreshFolder(new ArrayList<>());
            return;
        }
        LanDeviceBean lanDeviceBean = JsonUtil.fromJson(device, LanDeviceBean.class);
        String smbUrl;
        if (StringUtils.isEmpty(lanDeviceBean.getAccount()) || lanDeviceBean.isAnonymous()){
            smbUrl = "smb://"+lanDeviceBean.getIp()+"/";
        }else {
            smbUrl = "smb://"+lanDeviceBean.getAccount()+":"+lanDeviceBean.getPassword()+"@"+lanDeviceBean.getIp()+"/";
        }
        Observable.create((ObservableOnSubscribe<List<FolderBean>>) emitter -> {
            List<SmbBean2> beanList = traverseFolder(smbUrl);
            updateSmbDataBase(beanList);
            List<FolderBean> folderBeanList = getFolderFormDataBase();
            emitter.onNext(folderBeanList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FolderBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<FolderBean> beanList) {
                        getView().refreshFolder(beanList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().refreshFolder(new ArrayList<>());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    //遍历链接下所有视频文件
    private List<SmbBean2> traverseFolder(String smbUrl){
        try {
            SmbFile smbFile = new SmbFile(smbUrl);
            if (smbFile.isFile() && CommonUtils.isMediaFile(smbUrl)){
                SmbBean2 smbBean2 = new SmbBean2();
                smbBean2.setName(smbFile.getName());
                smbBean2.setUrl(smbUrl);
                List<SmbBean2> smbBean2List = new ArrayList<>();
                LogUtils.e("add smb video file: " + smbBean2.getUrl());
                smbBean2List.add(smbBean2);
                return smbBean2List;
            }else if (smbFile.isDirectory()){
                SmbFile[] smbFiles = smbFile.listFiles();
                List<SmbBean2> smbBean2List = new ArrayList<>();
                for (SmbFile file : smbFiles) {
                    smbBean2List.addAll(traverseFolder(file.getPath()));
                }
                return smbBean2List;
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    //更新数据库
    private void updateSmbDataBase(List<SmbBean2> beanList){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        sqLiteDatabase.delete(DataBaseInfo.getTableNames()[7], "", new String[]{});
        for (SmbBean2 smbBean2 : beanList){
            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[7][1], FileUtils.getDirName(smbBean2.getUrl()));
            values.put(DataBaseInfo.getFieldNames()[7][2], smbBean2.getUrl());
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[7], null,values);
        }
    }

    @Override
    public void deleteFolder(String folder) {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[7], "folder = ?", new String[]{folder});
        }).start();
    }

    private List<FolderBean> getFolderFormDataBase(){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        List<FolderBean> folderBeanList = new ArrayList<>();
        String sql = "SELECT folder,COUNT(*) as count FROM "+ DataBaseInfo.getTableNames()[7] + " GROUP BY folder";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
        while (cursor.moveToNext()){
            String path = cursor.getString(0);
            int count = cursor.getInt(1);
            folderBeanList.add(new FolderBean(path, count));
        }
        cursor.close();
        return folderBeanList;
    }
}
