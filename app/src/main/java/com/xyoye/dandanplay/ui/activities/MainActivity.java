package com.xyoye.dandanplay.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.base.BaseAppFragment;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.ui.fragment.HomeFragment;
import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import butterknife.BindView;
import libtorrent.Libtorrent;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView{

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;
    @BindView(R.id.navigationView)
    BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private BaseAppFragment previousFragment;

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
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (playFragment == null){
            playFragment = PlayFragment.newInstance();
            homeFragment = HomeFragment.newInstance();
            personalFragment = PersonalFragment.newInstance();
            mDelegate.loadMultipleRootFragment(R.id.fragment_container, 0, playFragment, homeFragment, personalFragment);
            previousFragment = playFragment;
        }
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return super.onCreateFragmentAnimator();
    }

    @Override
    public void initListener() {
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (homeFragment == null){
                        homeFragment = HomeFragment.newInstance();
                        mDelegate.showHideFragment(homeFragment);
                    }else {
                        mDelegate.showHideFragment(homeFragment, previousFragment);
                    }
                    previousFragment = homeFragment;
                    return true;
                case R.id.navigation_play:
                    if (playFragment == null){
                        playFragment = PlayFragment.newInstance();
                        mDelegate.showHideFragment(playFragment);
                    }else {
                        mDelegate.showHideFragment(playFragment, previousFragment);
                    }
                    previousFragment = playFragment;
                    return true;
                case R.id.navigation_personal:
                    if (personalFragment == null){
                        personalFragment = PersonalFragment.newInstance();
                        mDelegate.showHideFragment(personalFragment);
                    }else {
                        mDelegate.showHideFragment(personalFragment, previousFragment);
                    }
                    previousFragment = personalFragment;
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            for (Torrent torrent : IApplication.torrentList){
                if (Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusDownloading ||
                        Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusSeeding ){
                    ToastUtils.showShort("请先暂停下载任务再退出，否则无法保存下载进度");
                    return false;
                }
            }

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
