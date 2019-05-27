package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
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
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import butterknife.BindView;
import libtorrent.Libtorrent;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseMvpActivity<MainPresenter> implements MainView {
    @BindView(R.id.navigationView)
    BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private BaseAppFragment previousFragment;

    private MenuItem menuMainItem, menuLanItem, menuNetItem, menuSettingItem;

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

        //延迟500ms，防止与playFragment请求权限回调冲突
        navigationView.postDelayed(this::initTracker, 500);

        backupBlock();
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
            if (playFragment != null)
                playFragment.unregisterEventBus();
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
                    menuMainItem.setVisible(false);
                    menuLanItem.setVisible(false);
                    menuNetItem.setVisible(false);
                    menuSettingItem.setVisible(false);
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
                    menuMainItem.setVisible(true);
                    menuLanItem.setVisible(true);
                    menuNetItem.setVisible(true);
                    menuSettingItem.setVisible(false);
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
                    menuMainItem.setVisible(false);
                    menuLanItem.setVisible(false);
                    menuNetItem.setVisible(false);
                    menuSettingItem.setVisible(true);
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //暂停任务时保存任务进度
            for (Torrent torrent : IApplication.torrentList) {
                if (torrent.isDone()) continue;
                if (Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusDownloading ||
                        Libtorrent.torrentStatus(torrent.getId()) == Libtorrent.StatusSeeding) {
                    ToastUtils.showShort("请先暂停下载任务再退出，否则无法保存下载进度");
                    return false;
                }
            }

            if (System.currentTimeMillis() - touchTime > 1500) {
                ToastUtils.showShort("再按一次退出应用");
                touchTime = System.currentTimeMillis();
            } else {
                if (ServiceUtils.isServiceRunning(TorrentService.class))
                    ServiceUtils.stopService(TorrentService.class);
                Libtorrent.closeMetaInfo();
                Libtorrent.close();
                DataBaseManager.getInstance().closeDatabase();
                finish();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuMainItem = menu.findItem(R.id.menu_item_scan);
        menuLanItem = menu.findItem(R.id.menu_item_lan);
        menuNetItem = menu.findItem(R.id.menu_item_network);
        menuSettingItem = menu.findItem(R.id.menu_item_setting);
        menuMainItem.setVisible(true);
        menuLanItem.setVisible(true);
        menuNetItem.setVisible(true);
        menuSettingItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_scan:
                launchActivity(VideoScanActivity.class);
                break;
            case R.id.menu_item_lan:
                launchActivity(SmbActivity.class);
                //EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_LAN_FOLDER));
                break;
            case R.id.menu_item_network:
                new CommonEditTextDialog(this, R.style.Dialog, CommonEditTextDialog.NETWORK_LINK).show();
                break;
            case R.id.menu_item_setting:
                launchActivity(SettingActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playFragment != null){
            playFragment.unregisterEventBus();
        }
    }

    //备份弹幕屏蔽的数据库，仅对旧版本（3.3.2及之前）的用户进行一次备份
    private void backupBlock(){
        //已获得权限，即视为不是第一次安装
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED)) {
            //如果从未备份备份
            if (AppConfig.getInstance().isBackupNull()){
                LogUtils.d("start backup block");
                presenter.backupBlockData();
                AppConfig.getInstance().setBackupNotNull();
            }
        }else {
            AppConfig.getInstance().setBackupNotNull();
        }
    }

    @SuppressLint("CheckResult")
    private void initTracker(){
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        presenter.initTracker();
                    }
                });
    }
}
