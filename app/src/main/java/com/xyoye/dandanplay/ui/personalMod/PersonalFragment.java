package com.xyoye.dandanplay.ui.personalMod;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.mvp.impl.PersonalFragmentPresenterImpl;
import com.xyoye.dandanplay.utils.UserInfoShare;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragment extends BaseFragment<PersonalFragmentPresenter> implements PersonalFragmentView,View.OnClickListener {
    @BindView(R.id.user_info_rl)
    RelativeLayout userInfoRl;
    @BindView(R.id.login_rl)
    RelativeLayout loginRl;
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.login_tv)
    TextView loginTv;
    @BindView(R.id.register_tv)
    TextView registerTv;

    public static PersonalFragment newInstance(){
        return new PersonalFragment();
    }

    @NonNull
    @Override
    protected PersonalFragmentPresenter initPresenter() {
        return new PersonalFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_personal;
    }

    @Override
    public void initView() {

    }

    @Override
    public void changeView(){
        if (UserInfoShare.getInstance().isLogin()){
            userInfoRl.setVisibility(View.VISIBLE);
            loginRl.setVisibility(View.GONE);
            Glide.with(this)
                    .load(UserInfoShare.getInstance().getUserImage())
                    .into(userImageIv);
            userNameTv.setText(UserInfoShare.getInstance().getUserScreenName());
        }else {
            loginRl.setVisibility(View.VISIBLE);
            userInfoRl.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListener() {
        userInfoRl.setOnClickListener(this);
        loginTv.setOnClickListener(this);
        registerTv.setOnClickListener(this);
        userImageIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_info_rl:
                launchActivity(PersonalInfoActivity.class);
                break;
            case R.id.login_tv:
                launchActivity(LoginActivity.class);
                break;
            case R.id.register_tv:
                ToastUtils.showShort("此功能暂未开放");
                break;
            case R.id.user_image_iv:
                ToastUtils.showShort("此功能暂未开放");
                break;
        }
    }
}
