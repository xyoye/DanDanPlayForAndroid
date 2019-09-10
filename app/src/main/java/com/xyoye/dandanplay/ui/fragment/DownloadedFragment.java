package com.xyoye.dandanplay.ui.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.mvp.impl.DownloadedFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadedFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadedFragmentView;
import com.xyoye.dandanplay.ui.weight.dialog.TaskDownloadedFileDialog;
import com.xyoye.dandanplay.ui.weight.dialog.TaskDownloadedInfoDialog;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadedItem;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedFragment extends BaseMvpFragment<DownloadedFragmentPresenter> implements DownloadedFragmentView, TaskDownloadedInfoDialog.TaskDeleteListener {

    @BindView(R.id.task_rv)
    RecyclerView taskRv;

    private BaseRvAdapter<DownloadedTaskBean> taskRvAdapter;
    private List<DownloadedTaskBean> taskList;
    private SparseArray<TaskDownloadedFileDialog> dialogArray = new SparseArray<>();

    public static DownloadedFragment newInstance() {
        return new DownloadedFragment();
    }

    @NonNull
    @Override
    protected DownloadedFragmentPresenter initPresenter() {
        return new DownloadedFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_downloaded;
    }

    @Override
    public void initView() {
        taskList = new ArrayList<>();
        taskRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        taskRvAdapter = new BaseRvAdapter<DownloadedTaskBean>(taskList) {
            @NonNull
            @Override
            public AdapterItem<DownloadedTaskBean> onCreateItem(int viewType) {
                return new TaskDownloadedItem(dialogArray, DownloadedFragment.this);
            }
        };
        taskRv.setAdapter(taskRvAdapter);

        presenter.queryDownloadedTask();
    }

    @Override
    public void initListener() {

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

    }

    @Override
    public void updateTask(List<DownloadedTaskBean> taskList) {
        this.taskList.clear();
        this.taskList.addAll(taskList);
        taskRvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskDelete(int position, String taskHash, boolean withFile) {

        //删除系统中文件
        if (withFile) {
            FileUtils.deleteDir(taskList.get(position).getSaveDirPath());
        }

        //删除数据库中信息
        DataBaseManager.getInstance()
                .selectTable(14)
                .delete()
                .where(1, taskHash)
                .execute();
        DataBaseManager.getInstance()
                .selectTable(15)
                .delete()
                .where(1, taskHash)
                .execute();


        //内存中数据
        if (position > -1 && position < taskList.size())
            taskRvAdapter.removeItem(position);
    }

    public void updateTask() {
        presenter.queryDownloadedTask();
    }

    public void updateDanmu(int taskPosition, int taskFilePosition, String danmuPath, int episodeId) {
        if (taskPosition == -1 || taskFilePosition == -1)
            return;

        if (taskPosition < taskList.size()) {
            DownloadedTaskBean taskBean = taskList.get(taskPosition);
            if (taskFilePosition < taskBean.getFileList().size()) {
                DownloadedTaskBean.DownloadedTaskFileBean fileBean = taskBean.getFileList().get(taskFilePosition);
                fileBean.setDanmuPath(danmuPath);
                fileBean.setEpisode_id(episodeId);

                String hash = taskBean.getTorrentHash();
                DataBaseManager.getInstance()
                        .selectTable(15)
                        .update()
                        .param(4, danmuPath)
                        .param(5, episodeId)
                        .where(1, hash)
                        .postExecute();

                TaskDownloadedFileDialog downloadedFileDialog = dialogArray.get(taskPosition);
                if (downloadedFileDialog != null && downloadedFileDialog.isShowing()) {
                    downloadedFileDialog.updateFileList();
                }

                taskRvAdapter.notifyItemChanged(taskPosition);
            }
        }
    }
}
