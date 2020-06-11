package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.RemoteVideoBean;
import com.xyoye.dandanplay.mvp.presenter.RemotePresenter;
import com.xyoye.dandanplay.mvp.view.RemoteView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.DanmuUtils;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.gson.GsonFactory;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;
import com.xyoye.dandanplay.utils.net.service.RetrofitService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemotePresenterImpl extends BaseMvpPresenterImpl<RemoteView> implements RemotePresenter {
    private String baseUrl = "";
    private RetrofitService retrofitService;

    public RemotePresenterImpl(RemoteView view, LifecycleOwner lifecycleOwner) {
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
    public void getVideoList(String ip, int port, String authorization) {
        RetrofitService retrofitService = getRetrofitService(ip, port, authorization);
        retrofitService.getRemoteVideoList()
                .doOnSubscribe(new NetworkConsumer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommOtherDataObserver<List<RemoteVideoBean>>() {
                    @Override
                    public void onSuccess(List<RemoteVideoBean> videoBeanList) {
                        getView().hideLoading();
                        String auth = authorization == null ? " " : authorization;
                        AppConfig.getInstance().setRemoteLoginData(ip +";"+port+";"+auth);
                        if (videoBeanList == null){
                            videoBeanList = new ArrayList<>();
                        }
                        for (RemoteVideoBean videoBean : videoBeanList){
                            videoBean.setOriginUrl(baseUrl);
                        }
                        getView().refreshVideoList(videoBeanList);
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        getView().showError("远程访问获取视频列表失败 "+message);
                        getView().hideLoading();
                    }
                });
    }

    @Override
    public void bindRemoteDanmu(String hash, String danmuName) {

        retrofitService.downloadRemoteDanmu(hash)
                .doOnSubscribe(new NetworkConsumer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommOtherDataObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        if (responseBody == null){
                            ToastUtils.showShort("获取弹幕失败");
                            getView().hideLoading();
                        }else {
                            InputStream inputStream = responseBody.byteStream();

                            Scanner scanner = new Scanner(inputStream, "utf-8");

                            StringBuilder stringBuilder = new StringBuilder();
                            while(scanner.hasNext())
                                stringBuilder.append(scanner.nextLine());

                            //下载弹幕至默认下载文件夹
                            String folder = AppConfig.getInstance().getDownloadFolder()
                                    + Constants.DefaultConfig.danmuFolder;
                            DanmuUtils.saveDanmuSourceFormBiliBili(stringBuilder.toString(), danmuName, folder);

                            String danmuPath = AppConfig.getInstance().getDownloadFolder()
                                    + Constants.DefaultConfig.danmuFolder + "/"
                                    + danmuName + ".xml";

                            getView().showError("弹幕绑定完成：" + danmuPath);
                            getView().onDanmuBind(hash, danmuPath);
                            getView().hideLoading();
                        }
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        ToastUtils.showShort("当前视频无可用弹幕");
                        getView().hideLoading();
                    }
                });
    }

    private RetrofitService getRetrofitService(String ip, int port, String authorization){
        String newUrl = "http://" + ip + ":" + port + "/";
        if (newUrl.equals(baseUrl) && retrofitService != null){
            return retrofitService;
        }
        baseUrl = newUrl;
        retrofitService = null;
        retrofitService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient(authorization))
                .build()
                .create(RetrofitService.class);
        return retrofitService;
    }

    private OkHttpClient getOkHttpClient(String authorization) {
        OkHttpClient.Builder builder = OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        //添加token验证
        if (!StringUtils.isEmpty(authorization)){
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer "+ authorization);
                return chain.proceed(requestBuilder.build());
            });
        }
        return builder.build();
    }
}
