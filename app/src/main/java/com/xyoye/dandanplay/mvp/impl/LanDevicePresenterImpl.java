package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.mvp.presenter.LanDevicePresenter;
import com.xyoye.dandanplay.mvp.view.LanDeviceView;
import com.xyoye.dandanplay.utils.smb.FindLanDevicesTask;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;
import com.xyoye.dandanplay.utils.smb.cybergarage.util.FileUtil;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import jcifs.netbios.NbtAddress;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyy on 2018/11/19.
 */

public class LanDevicePresenterImpl extends BaseMvpPresenter<LanDeviceView> implements LanDevicePresenter {
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

    @SuppressLint("CheckResult")
    @Override
    public void authLan(LanDeviceBean deviceBean, int position, boolean isAdd){
        Observable.create((ObservableOnSubscribe<LanDeviceBean>) emitter -> {
            String smbIp;
            if (StringUtils.isEmpty(deviceBean.getAccount()) || deviceBean.isAnonymous()){
                smbIp = "smb://"+deviceBean.getIp()+"/";
            }else {
                smbIp = "smb://"+deviceBean.getAccount()+":"+deviceBean.getPassword()+"@"+deviceBean.getIp()+"/";
            }
            try {
                SmbFile smbFile = new SmbFile(smbIp);
                smbFile.listFiles();
                if (isAdd){
                    try {
                        //为新增设备添加设备名
                        NbtAddress nbtAddress = NbtAddress.getByName(deviceBean.getIp());
                        nbtAddress.firstCalledName();
                        deviceBean.setDeviceName(nbtAddress.nextCalledName());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                emitter.onNext(deviceBean);
            } catch (MalformedURLException urlException){
                urlException.printStackTrace();
                getView().showError("Url错误："+smbIp);
            } catch (SmbException e) {
                getView().showError("登陆设备失败："+e.getNtStatus());
                e.printStackTrace();
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
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
            SmbFile smbFile = new SmbFile(smbUrl);
            if (smbFile.isFile() && com.xyoye.dandanplay.utils.FileUtils.isMediaFile(smbUrl)){
                SmbBean smbBean = new SmbBean();
                smbBean.setName(smbFile.getName());
                smbBean.setUrl(smbUrl);
                List<SmbBean> smbBeanList = new ArrayList<>();
                TLog.e("add smb video file: " + smbBean.getUrl());
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
        } catch (MalformedURLException | SmbException e) {
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
