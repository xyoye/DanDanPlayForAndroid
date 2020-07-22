package com.xyoye.dandanplay.ui.fragment;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.mvp.impl.DownloadingFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadingFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadingFragmentView;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadingItem;
import com.xyoye.dandanplay.utils.TaskManageListener;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStateBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private TaskManageListener taskManageListener;
    private List<TaskStateBean> taskStateBeanList;

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
        taskStateBeanList = new ArrayList<>();

        taskRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        taskRv.setNestedScrollingEnabled(false);
        taskRv.setItemViewCacheSize(10);
        SimpleItemAnimator simpleItemAnimator = ((SimpleItemAnimator) taskRv.getItemAnimator());
        if (simpleItemAnimator != null)
            simpleItemAnimator.setSupportsChangeAnimations(false);

        taskRvAdapter = new BaseRvAdapter<TaskStateBean>(taskStateBeanList) {
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
                if (taskManageListener != null)
                    taskManageListener.pauseAllTask();
                break;
            case R.id.start_all_tv:
                if (taskManageListener != null)
                    taskManageListener.resumeAllTask();
                break;
        }
    }

    public void setTaskManageListener(TaskManageListener taskManageListener) {
        this.taskManageListener = taskManageListener;
    }

    public void updateAdapter(Collection<TaskStateBean> stateBeanList) {
        taskStateBeanList.clear();
        taskStateBeanList.addAll(stateBeanList);
        Collections.sort(taskStateBeanList);

        taskRvAdapter.notifyDataSetChanged();
        downloadingTaskNumberTv.setText(String.valueOf(taskRvAdapter.getItemCount()));
    }
}
