package com.xyoye.dandanplay.ui.activities.personal;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.event.TorrentServiceEvent;
import com.xyoye.dandanplay.mvp.impl.DownloadManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.service.TorrentService;
import com.xyoye.dandanplay.ui.activities.anime.TrackerActivity;
import com.xyoye.dandanplay.ui.fragment.DownloadedFragment;
import com.xyoye.dandanplay.ui.fragment.DownloadingFragment;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.utils.TabEntity;
import com.xyoye.dandanplay.utils.TaskManageListener;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentConfig;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngine;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadManagerActivity extends BaseMvpActivity<DownloadManagerPresenter> implements DownloadManagerView {
    public static final int TASK_DOWNLOADING_DANMU_BIND = 1001;
    public static final int TASK_DOWNLOADED_DANMU_BIND = 1002;

    @BindView(R.id.tab_layout)
    CommonTabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private List<Fragment> fragmentList;

    @NonNull
    @Override
    protected DownloadManagerPresenter initPresenter() {
        return new DownloadManagerPresenterImpl(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_download_manager;
    }

    @Override
    public void initView() {
        setTitle("下载管理");
        fragmentList = new ArrayList<>();
        DownloadedFragment downloadedFragment = DownloadedFragment.newInstance();
        DownloadingFragment downloadingFragment = DownloadingFragment.newInstance();
        fragmentList.add(downloadingFragment);
        fragmentList.add(downloadedFragment);

        initViewPager();

        int position = getIntent().getIntExtra("fragment_position", 0);
        tabLayout.setCurrentTab(position == 0 ? 0 : 1);
        viewPager.setCurrentItem(position == 0 ? 0 : 1);

        if (ServiceUtils.isServiceRunning(TorrentService.class)) {
            startNewTask();
        } else {
            startTorrentService(null, null);
            presenter.observeService();
        }
    }

    @Override
    public void initListener() {
        TaskManageListener taskManageListener = new TaskManageListener() {
            @Override
            public void pauseTask(String taskHash) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(taskHash);
                if (torrentTask != null)
                    torrentTask.pause();
            }

            @Override
            public void resumeTask(String taskHash) {
                boolean isWifiLimit = TorrentConfig.getInstance().isDownloadOnlyWifi() && !NetworkUtils.isWifiConnected();
                if (isWifiLimit) {
                    ToastUtils.showShort("仅限WIFI下载");
                } else {
                    TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(taskHash);
                    if (torrentTask != null)
                        torrentTask.resume();
                }
            }

            @Override
            public void deleteTask(String taskHash, boolean withFile) {
                TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(taskHash);
                if (torrentTask != null)
                    torrentTask.remove(false);
            }

            @Override
            public void pauseAllTask() {
                TorrentEngine.getInstance().pauseAll();
            }

            @Override
            public void resumeAllTask() {
                boolean isWifiLimit = TorrentConfig.getInstance().isDownloadOnlyWifi() && !NetworkUtils.isWifiConnected();
                if (isWifiLimit) {
                    ToastUtils.showShort("仅限WIFI下载");
                } else {
                    TorrentEngine.getInstance().resumeAll();
                }
            }
        };

        DownloadingFragment downloadingFragment = (DownloadingFragment) fragmentList.get(0);
        downloadingFragment.setTaskManageListener(taskManageListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_download:
                new CommonDialog.Builder(DownloadManagerActivity.this)
                        .hideCancel()
                        .setAutoDismiss()
                        .build()
                        .show(getResources().getString(R.string.about_download), "关于下载", "确定", "");
                break;
            case R.id.tracker_manager:
                startActivity(new Intent(DownloadManagerActivity.this, TrackerActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showLoading() {
        //showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        //dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    private void initViewPager() {
        ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
        mTabEntities.add(new TabEntity("下载中", 0, 0));
        mTabEntities.add(new TabEntity("已完成", 0, 0));
        tabLayout.setTabData(mTabEntities);

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        DownloadFragmentAdapter fragmentAdapter = new DownloadFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == TASK_DOWNLOADING_DANMU_BIND) {

            } else if (requestCode == TASK_DOWNLOADED_DANMU_BIND) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //没有下载任务在执行，关闭服务
        if (TorrentEngine.getInstance().isAllowExit()) {
            stopService(new Intent(this, TorrentService.class));
        }
    }

    /**
     * 开启新任务
     */
    @Override
    public void startNewTask() {
        Torrent torrent = getIntent().getParcelableExtra("download_data");
        if (torrent != null) {
            startTorrentService(TorrentService.Action.ACTION_ADD_TORRENT, torrent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(TorrentServiceEvent event) {
        DownloadingFragment downloadingFragment = (DownloadingFragment) fragmentList.get(0);
        downloadingFragment.updateAdapter(TorrentService.taskStateMap.values());
        if (event.isTaskFinish()) {
            DownloadedFragment downloadedFragment = (DownloadedFragment) fragmentList.get(1);
            downloadedFragment.updateTask();
        }
    }


    /**
     * 开启下载服务
     */
    private void startTorrentService(String action, Torrent torrent) {
        Intent intent = new Intent(this, TorrentService.class);

        if (!TextUtils.isEmpty(action)) {
            intent.setAction(action);
        }
        if (torrent != null) {
            intent.putExtra(TorrentService.IntentTag.ADD_TASK_TORRENT, torrent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private class DownloadFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> list;

        private DownloadFragmentAdapter(FragmentManager supportFragmentManager, List<Fragment> list) {
            super(supportFragmentManager);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
