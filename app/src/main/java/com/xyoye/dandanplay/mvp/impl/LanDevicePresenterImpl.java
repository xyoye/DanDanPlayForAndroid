package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.LanDevicePresenter;
import com.xyoye.dandanplay.mvp.view.LanDeviceView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.smb.FindLanDevicesTask;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jcifs.Address;
import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyy on 2018/11/19.
 */

public class LanDevicePresenterImpl extends BaseMvpPresenterImpl<LanDeviceView> implements LanDevicePresenter {
    private Context mContext;

    public LanDevicePresenterImpl(LanDeviceView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        mContext = getView().getContext();
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
    public void getLanDevices() {
        io.reactivex.Observable.create((ObservableOnSubscribe<List<LanDeviceBean>>) emitter -> {
            String localIp = new LocalIPUtil(mContext).getLocalIp();
            if (!StringUtils.isEmpty(localIp)){
                new FindLanDevicesTask(localIp, deviceList -> {
                    Collections.sort(deviceList);
                    emitter.onNext(deviceList);
                }).run();
            }else {
                getView().showError("获取手机IP地址失败");
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceBeanList -> getView().refreshDevices(deviceBeanList));
    }

    private CIFSContext context;
    @SuppressLint("CheckResult")
    @Override
    public void authLan(LanDeviceBean deviceBean, int position, boolean isAdd){
        Observable
                .create((ObservableOnSubscribe<LanDeviceBean>) emitter -> {
                    try {
                        NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(deviceBean.getDomain(), deviceBean.getAccount(), deviceBean.getPassword());
                        context = SingletonContext.getInstance().withCredentials(auth);
                        Address address = context.getNameServiceClient().getByName(deviceBean.getIp());
                        context.getTransportPool().logon(context, address);
                        SmbFile smbFile = new SmbFile("smb://"+deviceBean.getIp(), context);
                        smbFile.listFiles();
                        if (isAdd){
                            //为新增设备添加设备名
                            try {
                                Address nameAddress = SingletonContext.getInstance().getNameServiceClient().getByName(deviceBean.getIp());
                                nameAddress.firstCalledName();
                                deviceBean.setDeviceName(nameAddress.nextCalledName(SingletonContext.getInstance()));
                            }catch (UnknownHostException e){
                                deviceBean.setDeviceName("UnKnow");
                            }
                        }
                        emitter.onNext(deviceBean);
                    } catch (SmbException e) {
                        getView().showError("登陆设备失败：请检查账号密码或防火墙："+SmbException.getMessageByCode(e.getNtStatus()));
                        e.printStackTrace();
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceBean1 -> {
                    if (!isAdd){
                        getView().authSuccess(deviceBean1, position);
                    }
                    else{
                        getView().addDevice(deviceBean1);
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void searchVideo(String smbUrl){
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            List<SmbBean> beanList = traverseFolder(smbUrl);
            updateSmbDataBase(beanList);
            emitter.onNext(beanList.size());
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    ToastUtils.showShort("搜索到"+integer+"个文件");
                    getView().searchOver();
                });
    }

    //遍历链接下所有视频文件
    private List<SmbBean> traverseFolder(String smbUrl){
        try {
            SmbFile smbFile = new SmbFile(smbUrl, context);
            if (smbFile.isFile() && CommonUtils.isMediaFile(smbUrl)){
                SmbBean smbBean = new SmbBean();
                smbBean.setName(smbFile.getName());
                smbBean.setUrl(smbUrl);
                List<SmbBean> smbBeanList = new ArrayList<>();
                LogUtils.e("add smb video file: " + smbBean.getUrl());
                smbBeanList.add(smbBean);
                return smbBeanList;
            }else if (smbFile.isDirectory()){
                SmbFile[] smbFiles = smbFile.listFiles();
                List<SmbBean> smbBeanList = new ArrayList<>();
                for (SmbFile file : smbFiles) {
                    smbBeanList.addAll(traverseFolder(file.getPath()));
                }
                return smbBeanList;
            }
        } catch (SmbException | MalformedURLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    //更新数据库
    private void updateSmbDataBase(List<SmbBean> beanList){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        sqLiteDatabase.delete(DataBaseInfo.getTableNames()[7], "", new String[]{});
        for (SmbBean smbBean : beanList){
            ContentValues values = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[7][1], FileUtils.getDirName(smbBean.getUrl()));
            values.put(DataBaseInfo.getFieldNames()[7][2], smbBean.getUrl());
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[7], null,values);
        }
    }
}
