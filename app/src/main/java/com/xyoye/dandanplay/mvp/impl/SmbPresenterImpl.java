package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.mvp.presenter.SmbPresenter;
import com.xyoye.dandanplay.mvp.view.SmbView;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.smb.LocalIPUtil;
import com.xyoye.dandanplay.utils.smb.SearchSmbDevicesTask;
import com.xyoye.dandanplay.utils.smb.SmbServer;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jcifs.Address;
import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbPresenterImpl extends BaseMvpPresenterImpl<SmbView> implements SmbPresenter {
    private Disposable queryDeviceDis;

    private CIFSContext cifsContext;
    private String rootUrl;
    private String dirUrl;

    public SmbPresenterImpl(SmbView view, Lifeful lifeful) {
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
                .postExecute(new QueryAsyncResultCallback<List<SmbBean>>(getLifeful()) {
                    @Override
                    public List<SmbBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<SmbBean> deviceList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            SmbBean deviceBean = new SmbBean();
                            deviceBean.setName(cursor.getString(1));
                            deviceBean.setNickName(cursor.getString(2));
                            deviceBean.setUrl(cursor.getString(3));
                            deviceBean.setAccount(cursor.getString(4));
                            deviceBean.setPassword(cursor.getString(5));
                            deviceBean.setDomain(cursor.getString(6));
                            deviceBean.setAnonymous(cursor.getInt(7) == 1);
                            deviceBean.setSmbType(Constants.SmbType.SQL_DEVICE);
                            deviceList.add(deviceBean);
                        }
                        return deviceList;
                    }

                    @Override
                    public void onResult(List<SmbBean> result) {
                        getView().refreshSqlDevice(result);
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void queryLanDevice() {
        getView().showLoading();
        queryDeviceDis = Observable.create((ObservableOnSubscribe<List<SmbBean>>) emitter -> {
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
    public void addSqlDevice(SmbBean smbBean) {
        DataBaseManager.getInstance()
                .selectTable("smb_device")
                .query()
                .where("device_ip", smbBean.getUrl())
                .postExecute(cursor -> {
                    if (cursor.getCount() > 0) {
                        DataBaseManager.getInstance()
                                .selectTable("smb_device")
                                .update()
                                .param("device_name", smbBean.getName())
                                .param("device_nick_name", smbBean.getNickName())
                                .param("device_user_name", smbBean.getAccount())
                                .param("device_user_password", smbBean.getPassword())
                                .param("device_user_domain", smbBean.getDomain())
                                .param("device_anonymous", smbBean.isAnonymous() ? 1 : 0)
                                .where("device_ip", smbBean.getUrl())
                                .postExecute();
                    } else {
                        String deviceName = StringUtils.isEmpty(smbBean.getName()) ? "UnKnow" : smbBean.getName();
                        DataBaseManager.getInstance()
                                .selectTable("smb_device")
                                .insert()
                                .param("device_name", deviceName)
                                .param("device_ip", smbBean.getUrl())
                                .param("device_user_name", smbBean.getAccount())
                                .param("device_user_password", smbBean.getPassword())
                                .param("device_user_domain", smbBean.getDomain())
                                .param("device_anonymous", smbBean.isAnonymous() ? 1 : 0)
                                .executeAsync();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void loginSmb(SmbBean smbBean, int position) {
        getView().showLoading();
        Observable.create((ObservableOnSubscribe<List<SmbBean>>) emitter -> {
            try {
                //组装URL
                String smbUrl;
                if (smbBean.isAnonymous()) {
                    smbUrl = "smb://" + smbBean.getUrl() + "/";
                } else {
                    smbUrl = "smb://" + smbBean.getAccount() + ":" + smbBean.getPassword() + "@" + smbBean.getUrl() + "/";
                }

                //登录验证信息
                NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(smbBean.getDomain(), smbBean.getAccount(), smbBean.getPassword());
                //登录配置信息
                Properties properties = new Properties();
                properties.setProperty("jcifs.smb.client.responseTimeout", "5000");
                PropertyConfiguration configuration = new PropertyConfiguration(properties);

                cifsContext = new BaseContext(configuration).withCredentials(auth);
                Address address = cifsContext.getNameServiceClient().getByName(smbBean.getUrl());
                cifsContext.getTransportPool().logon(cifsContext, address);

                //使用listFiles验证登录，并获取子文件集合
                List<SmbBean> fileBeanList = new ArrayList<>();
                SmbFile rootFile = new SmbFile(smbUrl, cifsContext);
                for (SmbFile smbFile : rootFile.listFiles()) {
                    SmbBean fileBean = new SmbBean();
                    fileBean.setName(smbFile.getName());
                    fileBean.setUrl(smbFile.getPath());
                    fileBean.setSmbType(smbFile.isDirectory()
                            ? Constants.SmbType.FOLDER
                            : Constants.SmbType.FILE);
                    fileBeanList.add(fileBean);
                }

                //父目录文件夹路径
                dirUrl = smbUrl;
                //根目录文件夹路径
                rootUrl = dirUrl;

                emitter.onNext(fileBeanList);
            } catch (SmbException e) {
                getView().showError("登陆设备失败：请检查账号密码或防火墙：" + SmbException.getMessageByCode(e.getNtStatus()));
                e.printStackTrace();
            }
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SmbBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<SmbBean> fileBeans) {
                        getView().LoginSuccess(position, smbBean);
                        getView().refreshSmbFile(fileBeans, dirUrl);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        getView().hideLoading();
                    }
                });
    }

    @Override
    public void listSmbFolder(SmbBean smbBean) {
        getView().showLoading();
        Observable.create((ObservableOnSubscribe<List<SmbBean>>) emitter -> {
            try {
                List<SmbBean> fileBeanList = new ArrayList<>();
                SmbFile parentFile = new SmbFile(smbBean.getUrl(), cifsContext);
                for (SmbFile smbFile : parentFile.listFiles()) {
                    SmbBean fileBean = new SmbBean();
                    fileBean.setName(smbFile.getName());
                    fileBean.setUrl(smbFile.getPath());
                    fileBean.setSmbType(smbFile.isDirectory()
                            ? Constants.SmbType.FOLDER
                            : Constants.SmbType.FILE);
                    fileBeanList.add(fileBean);
                }

                Collections.sort(fileBeanList, (o1, o2) -> o1.getSmbType() - o2.getSmbType());

                //父目录文件夹路径
                dirUrl = smbBean.getUrl();

                emitter.onNext(fileBeanList);
            } catch (SmbException e) {
                getView().showError("获取文件列表失败" + SmbException.getMessageByCode(e.getNtStatus()));
                e.printStackTrace();
            }
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SmbBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<SmbBean> fileBeans) {
                        getView().refreshSmbFile(fileBeans, dirUrl);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        getView().hideLoading();
                    }
                });
    }

    @Override
    public void openSmbFile(SmbBean smbBean) {
        if (!CommonUtils.isMediaFile(smbBean.getUrl())) {
            ToastUtils.showShort("不是可播放的视频文件");
            return;
        }
        if (!ServiceUtils.isServiceRunning(SmbService.class)) {
            ToastUtils.showShort("共享服务未启动，无法播放");
            return;
        }
        try {
            SmbFile smbFile = new SmbFile(smbBean.getUrl(), cifsContext);
            SmbServer.setPlaySmbFile(smbFile);

            String httpUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT + "/";
            String videoUrl = httpUrl + smbBean.getUrl().replace("smb://", "smb=");

            PlayerManagerActivity.launchPlayerSmb(
                    getView().getContext(),
                    FileUtils.getFileNameNoExtension(videoUrl),
                    videoUrl
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ToastUtils.showShort("无法创建可播放视频流");
        }
    }

    @Override
    public void returnParentFolder() {
        if (dirUrl.equals(rootUrl)) {
            querySqlDevice();
        } else {
            String parentUrl = dirUrl;
            if (parentUrl.endsWith("/")) {
                parentUrl = parentUrl.substring(0, parentUrl.length() - 1);
            }
            int lastIndex = parentUrl.lastIndexOf("/");
            parentUrl = parentUrl.substring(0, lastIndex) + "/";

            SmbBean smbBean = new SmbBean();
            smbBean.setUrl(parentUrl);
            listSmbFolder(smbBean);
        }
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
