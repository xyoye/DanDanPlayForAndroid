package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.base.BaseAppFragment;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.ListFolderEvent;
import com.xyoye.dandanplay.mvp.impl.MainPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.ui.fragment.HomeFragment;
import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import libtorrent.Libtorrent;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {
    public final static int SELECT_FOLDER = 103;

    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;
    @BindView(R.id.navigationView)
    BottomNavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private HomeFragment homeFragment;
    private PlayFragment playFragment;
    private PersonalFragment personalFragment;
    private BaseAppFragment previousFragment;

    private MenuItem menuMainItem;
    private int fragFlag = 1;

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
        if (playFragment == null) {
            playFragment = PlayFragment.newInstance();
            homeFragment = HomeFragment.newInstance();
            personalFragment = PersonalFragment.newInstance();
            mDelegate.loadMultipleRootFragment(R.id.fragment_container, 1, homeFragment, playFragment, personalFragment);
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
                    if (homeFragment == null) {
                        homeFragment = HomeFragment.newInstance();
                        mDelegate.showHideFragment(homeFragment);
                    } else {
                        mDelegate.showHideFragment(homeFragment, previousFragment);
                    }
                    previousFragment = homeFragment;
                    menuMainItem.setVisible(false);
                    fragFlag = 0;
                    return true;
                case R.id.navigation_play:
                    if (playFragment == null) {
                        playFragment = PlayFragment.newInstance();
                        mDelegate.showHideFragment(playFragment);
                    } else {
                        mDelegate.showHideFragment(playFragment, previousFragment);
                    }
                    previousFragment = playFragment;
                    menuMainItem.setVisible(true);
                    menuMainItem.setTitle("添加文件夹");
                    menuMainItem.setIcon(R.drawable.ic_add);
                    fragFlag = 1;
                    return true;
                case R.id.navigation_personal:
                    if (personalFragment == null) {
                        personalFragment = PersonalFragment.newInstance();
                        mDelegate.showHideFragment(personalFragment);
                    } else {
                        mDelegate.showHideFragment(personalFragment, previousFragment);
                    }
                    previousFragment = personalFragment;
                    menuMainItem.setVisible(true);
                    menuMainItem.setTitle("设置");
                    menuMainItem.setIcon(R.drawable.ic_settings_white);
                    fragFlag = 2;
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

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
                finish();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuMainItem = menu.findItem(R.id.menu_item_mian);
        menuMainItem.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_mian:
                if (fragFlag == 1){
                    Intent intent = new Intent(this, FileManagerActivity.class);
                    intent.putExtra("file_type", FileManagerActivity.FILE_FOLDER);
                    startActivityForResult(intent, SELECT_FOLDER);
                }else if (fragFlag == 2){
                    launchActivity(SettingActivity.class);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FOLDER) {
                String folderPath = data.getStringExtra("folder");
                EventBus.getDefault().post(new ListFolderEvent(folderPath));
            }
        }
    }


}
