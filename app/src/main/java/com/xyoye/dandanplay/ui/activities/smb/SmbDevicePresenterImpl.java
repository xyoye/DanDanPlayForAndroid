package com.xyoye.dandanplay.ui.activities.smb;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;
import com.xyoye.dandanplay.utils.smb.SearchSmbDevicesTask;
import com.xyoye.smb.SmbManager;
import com.xyoye.smb.exception.SmbLinkException;
import com.xyoye.smb.info.SmbLinkInfo;
import com.xyoye.smb.info.SmbType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbDevicePresenterImpl extends BaseMvpPresenterImpl<SmbDeviceView> implements SmbDevicePresenter {
    private Disposable queryDeviceDis;

    SmbDevicePresenterImpl(SmbDeviceView view, Lifeful lifeful) {
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
        if (queryDeviceDis != null)
            queryDeviceDis.dispose();
    }

    @SuppressLint("CheckResult")
    @Override
    public void querySqlDevice() {
        DataBaseManager.getInstance()
                .selectTable("smb_device")
                .query()
                .postExecute(new QueryAsyncResultCallback<List<SmbDeviceBean>>(getLifeful()) {
                    @Override
                    public List<SmbDeviceBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<SmbDeviceBean> deviceList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            SmbDeviceBean deviceBean = new SmbDeviceBean();
                            deviceBean.setName(cursor.getString(1));
                            deviceBean.setNickName(cursor.getString(2));
                            deviceBean.setUrl(cursor.getString(3));
                            deviceBean.setAccount(cursor.getString(4));
                            deviceBean.setPassword(cursor.getString(5));
                            deviceBean.setDomain(cursor.getString(6));
                            deviceBean.setAnonymous(cursor.getInt(7) == 1);
                            deviceBean.setRootFolder(cursor.getString(8));
                            deviceBean.setSmbType(Constants.SmbSourceType.SQL_DEVICE);
                            deviceList.add(deviceBean);
                        }
                        return deviceList;
                    }

                    @Override
                    public void onResult(List<SmbDeviceBean> result) {
                        getView().refreshSqlDevice(result);
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void queryLanDevice() {
        getView().showLoading();
        queryDeviceDis = Observable.create((ObservableOnSubscribe<List<SmbDeviceBean>>) emitter -> {
            String localIp = new LocalIPUtil(getView().getContext()).getLocalIp();
            if (!StringUtils.isEmpty(localIp)) {
                new SearchSmbDevicesTask(localIp, deviceList -> {
                    Collections.sort(deviceList);
                    emitter.onNext(deviceList);
                }).run();
            } else {
                getView().showError("获取手机IP地址失败");
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(smbBeans -> getView().refreshLanDevice(smbBeans));
    }

    @Override
    public void addSqlDevice(SmbDeviceBean smbDeviceBean) {
        DataBaseManager.getInstance()
                .selectTable("smb_device")
                .query()
                .where("device_ip", smbDeviceBean.getUrl())
                .postExecute(cursor -> {
                    if (cursor.getCount() > 0) {
                        DataBaseManager.getInstance()
                                .selectTable("smb_device")
                                .update()
                                .param("device_name", smbDeviceBean.getName())
                                .param("device_nick_name", smbDeviceBean.getNickName())
                                .param("device_user_name", smbDeviceBean.getAccount())
                                .param("device_user_password", smbDeviceBean.getPassword())
                                .param("device_user_domain", smbDeviceBean.getDomain())
                                .param("device_share", smbDeviceBean.getRootFolder())
                                .param("device_anonymous", smbDeviceBean.isAnonymous() ? 1 : 0)
                                .where("device_ip", smbDeviceBean.getUrl())
                                .postExecute();
                    } else {
                        String deviceName = StringUtils.isEmpty(smbDeviceBean.getName()) ? "UnKnow" : smbDeviceBean.getName();
                        DataBaseManager.getInstance()
                                .selectTable("smb_device")
                                .insert()
                                .param("device_name", deviceName)
                                .param("device_ip", smbDeviceBean.getUrl())
                                .param("device_user_name", smbDeviceBean.getAccount())
                                .param("device_user_password", smbDeviceBean.getPassword())
                                .param("device_user_domain", smbDeviceBean.getDomain())
                                .param("device_share", smbDeviceBean.getRootFolder())
                                .param("device_anonymous", smbDeviceBean.isAnonymous() ? 1 : 0)
                                .executeAsync();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void loginSmbDevice(SmbDeviceBean smbDeviceBean, SmbType smbType) {
        SmbLinkInfo smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setAccount(smbDeviceBean.getAccount());
        smbLinkInfo.setIP(smbDeviceBean.getUrl());
        smbLinkInfo.setAccount(smbDeviceBean.getAccount());
        smbLinkInfo.setPassword(smbDeviceBean.getPassword());
        smbLinkInfo.setAnonymous(smbDeviceBean.isAnonymous());
        smbLinkInfo.setRootFolder(smbDeviceBean.getRootFolder());

        if (smbType == SmbType.SMBJ && TextUtils.isEmpty(smbLinkInfo.getRootFolder())){
            getView().showError("错误！使用SMBJ登录时Share（根目录）不能为空");
            return;
        }

        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            SmbManager smbManager = SmbManager.getInstance();
            if (smbManager.linkStart(smbType, smbLinkInfo))
                emitter.onNext(true);
            else
                emitter.onError(smbManager.getException());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        getView().loginSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof SmbLinkException){
                            SmbLinkException exception = (SmbLinkException)e;
                            String errorMsg = "";
                            if (exception.getDetailExceptions() != null && exception.getDetailExceptions().size() > 0){
                                errorMsg = "\n" + exception.getDetailExceptions().get(0).getErrorMsg();
                            }
                            ToastUtils.showShort("登录失败，试试切换连接工具"+errorMsg);
                        }else {
                            ToastUtils.showShort("登录失败，试试切换连接工具\n"+e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @Override
    public void removeSqlDevice(String url) {
        DataBaseManager.getInstance()
                .selectTable("smb_device")
                .delete()
                .where("device_ip", url)
                .postExecute();
    }
}
