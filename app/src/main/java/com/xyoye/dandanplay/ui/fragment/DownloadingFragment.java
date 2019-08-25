package com.xyoye.dandanplay.ui.fragment;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.mvp.impl.DownloadingFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadingFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadingFragmentView;
import com.xyoye.dandanplay.torrent.info.TaskStateBean;
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivity;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadingItem;
import com.xyoye.dandanplay.utils.TaskManageListener;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadingFragment extends BaseMvpFragment<DownloadingFragmentPresenter> implements DownloadingFragmentView {

    @BindView(R.id.downloading_task_number_tv)
    TextView downloadingTaskNumberTv;
    @BindView(R.id.task_rv)
    RecyclerView taskRv;

    private BaseRvAdapter<TaskStateBean> taskRvAdapter;
    private Handler mHandler = IApplication.getMainHandler();
    private TaskManageListener taskManageListener;
    private DownloadManagerActivity managerActivity;

    public static DownloadingFragment newInstance() {
        return new DownloadingFragment();
    }

    @NonNull
    @Override
    protected DownloadingFragmentPresenter initPresenter() {
        return new DownloadingFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_downloading;
    }

    @Override
    public void initView() {
        managerActivity = (DownloadManagerActivity)mContext;

        taskRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        taskRv.setNestedScrollingEnabled(false);
        taskRv.setItemViewCacheSize(10);
        SimpleItemAnimator simpleItemAnimator = ((SimpleItemAnimator)taskRv.getItemAnimator());
        if (simpleItemAnimator != null)
            simpleItemAnimator.setSupportsChangeAnimations(false);

        taskRvAdapter = new BaseRvAdapter<TaskStateBean>(managerActivity.getTaskList()) {
            @NonNull
            @Override
            public AdapterItem<TaskStateBean> onCreateItem(int viewType) {
                return new TaskDownloadingItem(taskManageListener);
            }
        };
        taskRv.setAdapter(taskRvAdapter);

    }

    @Override
    public void initListener() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    @OnClick({R.id.pause_all_tv, R.id.start_all_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pause_all_tv:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_PAUSE, -1));
                break;
            case R.id.start_all_tv:
                EventBus.getDefault().post(new TorrentEvent(TorrentEvent.EVENT_ALL_START, -1));
                break;
        }
    }

    public void setTaskManageListener(TaskManageListener taskManageListener){
        this.taskManageListener = taskManageListener;
    }

    public void updateAdapter(){
        taskRvAdapter.notifyDataSetChanged();
    }
}
