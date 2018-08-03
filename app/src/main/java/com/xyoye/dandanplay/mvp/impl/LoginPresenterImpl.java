package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.mvp.presenter.LoginPresenter;
import com.xyoye.dandanplay.mvp.view.LoginView;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.TokenShare;
import com.xyoye.dandanplay.utils.UserInfoShare;

import java.util.Map;

/**
 * Created by YE on 2018/7/22.
 */

public class LoginPresenterImpl extends BaseMvpPresenter<LoginView> implements LoginPresenter {

    public LoginPresenterImpl(LoginView view, Lifeful lifeful) {
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
    public void login(LoginParam param){
        param.setAppId(KeyUtil.getAppId(getView().getPersonalContext()));
        param.setUnixTimestamp(System.currentTimeMillis()/1000);
        param.buildHash(getView().getPersonalContext());
        PersonalBean.login(param, new CommJsonObserver<PersonalBean>() {
            @Override
            public void onSuccess(PersonalBean personalBean) {
                UserInfoShare.getInstance().setLogin(true);
                UserInfoShare.getInstance().saveUserScreenName(personalBean.getScreenName());
                UserInfoShare.getInstance().saveUserName(param.getUserName());
                UserInfoShare.getInstance().saveUserImage(personalBean.getProfileImage());
                TokenShare.getInstance().saveToken(personalBean.getToken());
                ToastUtils.showShort("登录成功");
                getView().launchMain();
            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    private void login(final SHARE_MEDIA platform) {
        UMShareAPI.get(getView().getActivity()).getPlatformInfo(getView().getActivity(), platform, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                if (i == 2 && map != null) {
                    TLog.i(i + "_" + map.toString());
                    switch (platform) {
                        case QQ:
                            authorize(map.get("uid"), map.get("name"), "qq");
                            break;
                    }
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                ToastUtils.showShort("授权失败");
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                ToastUtils.showShort("授权失败");
            }
        });
    }

    private void authorize(final String authCode, final String nick, final String type) {
        // TODO: 2018/8/1 验证QQ用户
    }

}
