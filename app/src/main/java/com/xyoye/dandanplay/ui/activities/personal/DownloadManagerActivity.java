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

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.impl.DownloadManagerPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadManagerPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadManagerView;
import com.xyoye.dandanplay.torrent.TorrentEngine;
import com.xyoye.dandanplay.torrent.TorrentService;
import com.xyoye.dandanplay.torrent.TorrentTask;
import com.xyoye.dandanplay.torrent.info.TaskStateBean;
import com.xyoye.dandanplay.torrent.info.Torrent;
import com.xyoye.dandanplay.ui.activities.anime.TrackerActivity;
import com.xyoye.dandanplay.ui.fragment.DownloadedFragment;
import com.xyoye.dandanplay.ui.fragment.DownloadingFragment;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.TorrentFileCheckDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.TabEntity;
import com.xyoye.dandanplay.utils.TaskManageListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.libtorrent4j.TorrentInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadManagerActivity extends BaseMvpActivity<DownloadManagerPresenter> implements DownloadManagerView, TaskManageListener {
    public static final int TASK_DOWNLOADING_DANMU_BIND = 1001;
    public static final int TASK_DOWNLOADED_DANMU_BIND = 1002;

    @BindView(R.id.tab_layout)
    CommonTabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private List<Fragment> fragmentList;
    private List<TaskStateBean> mTaskStateList;
    private int selectedPosition = 0;

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
        mTaskStateList = new ArrayList<>();
        DownloadedFragment downloadedFragment = DownloadedFragment.newInstance();
        DownloadingFragment downloadingFragment = DownloadingFragment.newInstance();
        downloadingFragment.setTaskManageListener(this);
        fragmentList.add(downloadingFragment);
        fragmentList.add(downloadedFragment);

        initTabLayout();

        initViewPager();

        int position = getIntent().getIntExtra("fragment_position", 0);
        if (position == 0) {
            tabLayout.setCurrentTab(0);
        } else {
            tabLayout.setCurrentTab(1);
        }

        if (ServiceUtils.isServiceRunning(TorrentService.class)) {
            viewPager.post(() -> {
                mTaskStateList = TorrentService.taskStateCache.getAll();
                downloadingFragment.updateAdapter(mTaskStateList);
            });
            startNewTask();
        } else {
            startTorrentService(null, null);
            presenter.observeService();
        }
    }

    @Override
    public void initListener() {

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
        getMenuInflater().inflate(R.menu.menu_download_manager_v2, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }


    private void initTabLayout() {
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
    }

    private void initViewPager() {
        DownloadFragmentAdapter fragmentAdapter = new DownloadFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setCurrentItem(selectedPosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
                selectedPosition = position;
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
                updateDownloadingDanmu(data);
            } else if (requestCode == TASK_DOWNLOADED_DANMU_BIND) {
                updateDownloadedDanmu(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //没有下载任务在执行，关闭服务
        if (!TorrentEngine.getInstance().hasTasks()) {
            startTorrentService(TorrentService.Action.ACTION_SHUTDOWN, null);
        }
    }

    /**
     * 开启新任务
     */
    @Override
    public void startNewTask() {
        String animeTitle = getIntent().getStringExtra("anime_title");
        String torrentFilePath = getIntent().getStringExtra("torrent_file_path");

        if (!TextUtils.isEmpty(torrentFilePath)){
            try {
                TorrentInfo torrentInfo = new TorrentInfo(new File(torrentFilePath));
                //任务不存在则新增任务
                TorrentTask torrentTask = TorrentEngine.getInstance().getTask(torrentInfo.infoHash().toHex());
                if (torrentTask == null){
                    new TorrentFileCheckDialog(this, torrentInfo, priorityList -> {
                        Torrent torrent = new Torrent(
                                animeTitle,
                                torrentFilePath,
                                AppConfig.getInstance().getDownloadFolder(),
                                priorityList);
                        startTorrentService(TorrentService.Action.ACTION_ADD_TORRENT, torrent);
                    }).show();
                }else{
                    startTorrentService(null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.showShort("获取下载任务详情失败");
            }
        }
    }

    /**
     * 开启下载服务
     */
    private void startTorrentService(String action, Torrent torrent) {
        Intent intent = new Intent(this, TorrentService.class);

        if (!TextUtils.isEmpty(action)){
            intent.setAction(action);
        }
        if (torrent != null){
            intent.putExtra(TorrentService.IntentTag.ADD_TASK_TORRENT, torrent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void pauseTask(String taskHash) {
        TorrentEngine.getInstance().getTask(taskHash).pause();
    }

    @Override
    public void resumeTask(String taskHash) {
        TorrentEngine.getInstance().getTask(taskHash).resume();
    }

    @Override
    public void deleteTask(String taskHash, boolean withFile) {
        TorrentEngine.getInstance().getTask(taskHash).remove(withFile);
    }

    @Override
    public List<TaskStateBean> getTaskList() {
        return mTaskStateList;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTaskStateChangeEvent(TaskStateBean taskStateBean){
        Iterator iterator = mTaskStateList.iterator();
        int position = 0;
        //移除任务旧状态，并将新任务状态添加到相同位置
        while (iterator.hasNext()){
            TaskStateBean stateBean = (TaskStateBean)iterator.next();
            if (stateBean.torrentId.equals(taskStateBean.torrentId)){
                iterator.remove();
                break;
            }
            position ++;
        }
        mTaskStateList.add(position, taskStateBean);
        DownloadingFragment downloadingFragment = (DownloadingFragment)fragmentList.get(0);
        if (downloadingFragment != null){
            downloadingFragment.updateAdapter(mTaskStateList);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTaskRemovedEvent(String torrentHash){
        Iterator iterator = mTaskStateList.iterator();
        //移除任务旧状态，并将新任务状态添加到相同位置
        while (iterator.hasNext()){
            TaskStateBean stateBean = (TaskStateBean)iterator.next();
            if (stateBean.torrentId.equals(torrentHash)){
                iterator.remove();
                break;
            }
        }
        DownloadingFragment downloadingFragment = (DownloadingFragment)fragmentList.get(0);
        if (downloadingFragment != null){
            downloadingFragment.updateAdapter(mTaskStateList);
        }
    }

    /**
     * 更新下载中任务弹幕信息
     */
    private void updateDownloadingDanmu(Intent data){
        int episodeId = data.getIntExtra("episode_id", -1);
        String danmuPath = data.getStringExtra("path");
        String taskHash = data.getStringExtra("task_hash");
        int taskFilePosition = data.getIntExtra("task_file_position", -1);
        //更新下载中信息
        Torrent torrent = TorrentEngine.getInstance().getTask(taskHash).getTorrent();
        Torrent.TorrentFile torrentFile = torrent.getChildFileList().get(taskFilePosition);
        torrentFile.setDanmuEpisodeId(episodeId);
        torrentFile.setDanmuFilePath(danmuPath);
        //更新数据库中信息
        DataBaseManager.getInstance()
                .selectTable(17)
                .update()
                .where(1, taskHash)
                .where(2, torrentFile.getFilePath())
                .param(4,danmuPath)
                .param(5, episodeId)
                .execute();
    }

    /**
     * 更新下载完成任务弹幕信息
     */
    private void updateDownloadedDanmu(Intent data){
        String danmuPath = data.getStringExtra("path");
        int episodeId = data.getIntExtra("episode_id", -1);
        int taskPosition = data.getIntExtra("position", -1);
        int taskFilePosition = data.getIntExtra("task_file_position", -1);

        DownloadedFragment downloadedFragment = (DownloadedFragment)fragmentList.get(1);
        if (downloadedFragment != null){
            List<DownloadedTaskBean> downloadedTaskList = downloadedFragment.getTaskList();
            DownloadedTaskBean taskBean = downloadedTaskList.get(taskPosition);
            DownloadedTaskBean.DownloadedTaskFileBean fileBean = taskBean.getFileList().get(taskFilePosition);

            //更新UI中数据
            fileBean.setDanmuPath(danmuPath);
            fileBean.setEpisode_id(episodeId);
            downloadedFragment.updateTask();

            String torrentHash = taskBean.getTorrentHash();
            String torrentFilePath = fileBean.getFilePath();
            //更新数据库中信息
            DataBaseManager.getInstance()
                    .selectTable(17)
                    .update()
                    .where(1, torrentHash)
                    .where(2, torrentFilePath)
                    .param(4,danmuPath)
                    .param(5, episodeId)
                    .execute();
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
