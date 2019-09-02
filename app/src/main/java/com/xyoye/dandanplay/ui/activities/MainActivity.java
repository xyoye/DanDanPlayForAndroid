package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.torrent.TorrentService;
import com.xyoye.dandanplay.ui.activities.play.SmbActivity;
import com.xyoye.dandanplay.ui.fragment.HomeFragment;
import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.ui.weight.dialog.RemoteDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseMvpActivity<MainPresenter> implements MainView {
    @BindView(R.id.navigation_view)
    BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private BaseMvpFragment previousFragment;

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
        if (hasBackActionbar() && getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        setTitle("媒体库");
        navigationView.setSelectedItemId(R.id.navigation_play);
        switchFragment(PlayFragment.class);

        initPermission();
    }

    private void initPermission() {
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean granted) {
                        if (granted) {
                            presenter.initScanFolder();
                            presenter.initTracker();
                            if (playFragment != null) {
                                playFragment.initVideoData();
                            }
                        } else {
                            ToastUtils.showLong("未授予文件管理权限，无法扫描视频");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!IApplication.startCorrectlyFlag) {
            IApplication.startCorrectlyFlag = true;
            FragmentTransaction fragmentTransaction = getFragmentTransaction();
            if (playFragment != null)
                fragmentTransaction.remove(playFragment);
            if (homeFragment != null)
                fragmentTransaction.remove(homeFragment);
            if (personalFragment != null)
                fragmentTransaction.remove(personalFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void initListener() {
        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle("弹弹play");
                    switchFragment(HomeFragment.class);
                    menuSmbItem.setVisible(false);
                    menuNetItem.setVisible(false);
                    menuRemoteItem.setVisible(false);
                    return true;
                case R.id.navigation_play:
                    setTitle("媒体库");
                    switchFragment(PlayFragment.class);
                    menuSmbItem.setVisible(true);
                    menuNetItem.setVisible(true);
                    menuRemoteItem.setVisible(true);
                    return true;
                case R.id.navigation_personal:
                    setTitle("个人中心");
                    switchFragment(PersonalFragment.class);
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateFragmentEvent event) {
        if (event.getClazz() == PlayFragment.class && playFragment != null) {
            playFragment.refreshFolderData(event.getUpdateType());
        } else if (event.getClazz() == PersonalFragment.class && personalFragment != null) {
            personalFragment.initView();
        }
    }

    @SuppressLint("CommitTransaction")
    private FragmentTransaction getFragmentTransaction() {
        return getSupportFragmentManager().beginTransaction();
    }

    private void switchFragment(Class clazz) {
        if (previousFragment != null && clazz.isInstance(previousFragment)) {
            return;
        } else if (previousFragment != null) {
            getFragmentTransaction().hide(previousFragment).commit();
        }

        if (clazz == HomeFragment.class) {
            if (homeFragment == null) {
                homeFragment = HomeFragment.newInstance();
                getFragmentTransaction().add(R.id.fragment_container, homeFragment).commit();
                previousFragment = homeFragment;
            } else {
                getFragmentTransaction().show(homeFragment).commit();
                previousFragment = homeFragment;
            }
        } else if (clazz == PersonalFragment.class) {
            if (personalFragment == null) {
                personalFragment = PersonalFragment.newInstance();
                getFragmentTransaction().add(R.id.fragment_container, personalFragment).commit();
                previousFragment = personalFragment;
            } else {
                getFragmentTransaction().show(personalFragment).commit();
                previousFragment = personalFragment;
            }
        } else if (clazz == PlayFragment.class) {
            if (playFragment == null) {
                playFragment = PlayFragment.newInstance();
                getFragmentTransaction().add(R.id.fragment_container, playFragment).commit();
                previousFragment = playFragment;
            } else {
                getFragmentTransaction().show(playFragment).commit();
                previousFragment = playFragment;
            }
        }
    }
}
