package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseAppFragment;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.fragment.HomeFragment;
import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.ui.weight.dialog.RemoteDialog;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseMvpActivity<MainPresenter> implements MainView, PlayFragment.InitTrackerListener {
    @BindView(R.id.navigationView)
    BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private BaseAppFragment previousFragment;

    private MenuItem menuSmbItem, menuNetItem, menuRemoteItem;

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
        setTitle("媒体库");
        if (hasBackActionbar() && getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        navigationView.setSelectedItemId(R.id.navigation_play);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (playFragment == null) {
            playFragment = PlayFragment.newInstance();
            homeFragment = HomeFragment.newInstance();
            personalFragment = PersonalFragment.newInstance();
            mDelegate.loadMultipleRootFragment(R.id.fragment_container, 1, homeFragment, playFragment, personalFragment);
            previousFragment = playFragment;
        }
        if (navigationView != null){
            if (navigationView.getSelectedItemId() == R.id.navigation_play){
                if (playFragment != null){
                    playFragment.registerEventBus();
                }
            }
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
                    setTitle("弹弹play");
                    if (homeFragment == null) {
                        homeFragment = HomeFragment.newInstance();
                        mDelegate.showHideFragment(homeFragment);
                    } else {
                        mDelegate.showHideFragment(homeFragment, previousFragment);
                    }
                    previousFragment = homeFragment;
                    menuSmbItem.setVisible(false);
                    menuNetItem.setVisible(false);
                    menuRemoteItem.setVisible(false);
                    return true;
                case R.id.navigation_play:
                    setTitle("媒体库");
                    if (playFragment == null) {
                        playFragment = PlayFragment.newInstance();
                        mDelegate.showHideFragment(playFragment);
                    } else {
                        mDelegate.showHideFragment(playFragment, previousFragment);
                    }
                    playFragment.registerEventBus();
                    previousFragment = playFragment;
                    menuSmbItem.setVisible(true);
                    menuNetItem.setVisible(true);
                    menuRemoteItem.setVisible(true);
                    return true;
                case R.id.navigation_personal:
                    setTitle("个人中心");
                    if (personalFragment == null) {
                        personalFragment = PersonalFragment.newInstance();
                        mDelegate.showHideFragment(personalFragment);
                    } else {
                        mDelegate.showHideFragment(personalFragment, previousFragment);
                    }
                    previousFragment = personalFragment;
                    menuSmbItem.setVisible(false);
                    menuNetItem.setVisible(false);
                    menuRemoteItem.setVisible(false);
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (System.currentTimeMillis() - touchTime > 1500) {
                ToastUtils.showShort("再按一次退出应用");
                touchTime = System.currentTimeMillis();
            } else {
                if (ServiceUtils.isServiceRunning(TorrentService.class))
                    ServiceUtils.stopService(TorrentService.class);
                DataBaseManager.getInstance().closeDatabase();
                finish();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuSmbItem = menu.findItem(R.id.menu_item_smb);
        menuNetItem = menu.findItem(R.id.menu_item_network);
        menuRemoteItem = menu.findItem(R.id.menu_item_remote);
        menuSmbItem.setVisible(true);
        menuNetItem.setVisible(true);
        menuRemoteItem.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //局域网
            case R.id.menu_item_smb:
                launchActivity(SmbActivity.class);
                break;
            //串流弹窗
            case R.id.menu_item_network:
                new CommonEditTextDialog(this, CommonEditTextDialog.NETWORK_LINK).show();
                break;
            //远程访问
            case R.id.menu_item_remote:
                new RemoteDialog(this).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playFragment != null)
            playFragment.unregisterEventBus();
    }

    @Override
    public void onFragmentPermissionGranted() {
        presenter.initTracker();
    }
}
