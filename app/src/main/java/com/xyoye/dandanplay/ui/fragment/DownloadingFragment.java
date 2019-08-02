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
import com.xyoye.dandanplay.ui.weight.item.DownloadManagerItem;
import com.xyoye.dandanplay.utils.DownloadTaskUpdateListener;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
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

    private BaseRvAdapter<BtTask> taskRvAdapter;
    private Runnable refresh;
    private Handler mHandler = IApplication.getMainHandler();
    private DownloadTaskUpdateListener taskUpdateListener;

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
        taskRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        taskRv.setNestedScrollingEnabled(false);
        taskRv.setItemViewCacheSize(10);
        SimpleItemAnimator simpleItemAnimator = ((SimpleItemAnimator)taskRv.getItemAnimator());
        if (simpleItemAnimator != null)
            simpleItemAnimator.setSupportsChangeAnimations(false);

        taskRvAdapter = new BaseRvAdapter<BtTask>(IApplication.taskList) {
            @NonNull
            @Override
            public AdapterItem<BtTask> onCreateItem(int viewType) {
                return new DownloadManagerItem();
            }
        };
        taskRv.setAdapter(taskRvAdapter);

        initRefresh();
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

    private void initRefresh(){
        refresh = () -> {
            int completePosition = -1;
            //刷新任务
            for (int i=0; i<IApplication.taskList.size(); i++){
                BtTask task = IApplication.taskList.get(i);
                if (task == null)
                    break;
                //刷新未完成任务
                if (!task.isFinished()){
                    taskRvAdapter.notifyItemChanged(i);
                }
                //保存已完成任务信息
                else if (!task.isRefreshAfterFinish()){
                    task.setRefreshAfterFinish(true);
                    presenter.setTaskFinish(task);
                    completePosition = i;
                    break;
                }
            }
            //移除已完成任务
            if (completePosition != -1){
                String taskHash = IApplication.taskList.get(completePosition).getTorrent().getHash();
                IApplication.taskMap.remove(taskHash);
                IApplication.taskFinishHashList.add(taskHash);
                taskRvAdapter.removeItem(completePosition);
                if (taskUpdateListener != null){
                    taskUpdateListener.onTaskUpdate();
                }
            }
            //刷新任务数量
            int taskNum = IApplication.taskList.size();
            mContext.runOnUiThread(() -> downloadingTaskNumberTv.setText(String.valueOf(taskNum)));
            //每隔1s刷新一次
            mHandler.postDelayed(refresh,1000);
        };
        refresh.run();
    }

    public void setUpdateListener(DownloadTaskUpdateListener updateListener){
        this.taskUpdateListener = updateListener;
    }

    public void stopRefresh(){
        mHandler.removeCallbacks(refresh);
    }

    public void updateAdapter(){
        taskRvAdapter.notifyDataSetChanged();
    }
}
