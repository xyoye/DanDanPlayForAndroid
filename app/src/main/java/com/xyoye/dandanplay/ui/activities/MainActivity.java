package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.fragment.HomeFragment;
import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;
    @BindView(R.id.navigationView)
    BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private Fragment previousFragment;

    private long touchTime = 0;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_main;
    }

    @NonNull
    @Override
    protected MainPresenterImpl initPresenter() {
        return new MainPresenterImpl(this, this);
    }

    @Override
    public void initView() {
        setTitle("");

        navigationView.setSelectedItemId(R.id.navigation_play);
        showFragment(1);
    }

    @Override
    public void initListener() {
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showFragment(0);
                    return true;
                case R.id.navigation_play:
                    showFragment(1);
                    return true;
                case R.id.navigation_personal:
                    showFragment(2);
                    return true;
            }
            return false;
        });
    }

    public void showFragment(int index) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (previousFragment != null)
            fragmentTransaction.hide(previousFragment);
        switch (index){
            case 0:
                if (homeFragment == null){
                    homeFragment = HomeFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_container, homeFragment, HomeFragment.TAG);
                }else {
                    fragmentTransaction.show(homeFragment);
                }
                previousFragment = homeFragment;
                break;
            case 1:
                if (playFragment == null){
                    playFragment = PlayFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_container, playFragment, PlayFragment.TAG);
                }else {
                    fragmentTransaction.show(playFragment);
                }
                previousFragment = playFragment;
                break;
            case 2:
                if (personalFragment == null){
                    personalFragment = PersonalFragment.newInstance();
                    fragmentTransaction.add(R.id.fragment_container, personalFragment, PersonalFragment.TAG);
                }else {
                    fragmentTransaction.show(personalFragment);
                }
                previousFragment = personalFragment;
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - touchTime > 1500) {
                ToastUtils.showShort("再按一次退出应用");
                touchTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }
}
