package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.bean.event.TaskBindDanmuEndEvent;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadedFileItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadedDetailDialog extends Dialog {
    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.magnet_tv)
    TextView magnetTv;
    @BindView(R.id.file_rv)
    RecyclerView fileRv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private Context context;
    private int taskPosition;
    private DownloadedTaskBean taskBean;
    private BaseRvAdapter<DownloadedTaskBean.DownloadedTaskFileBean> fileAdapter;
    private Activity mActivity;
    private TaskDeleteListener taskDeleteListener;

    public TaskDownloadedDetailDialog(@NonNull Context context, int taskPosition, DownloadedTaskBean taskBean, TaskDeleteListener taskDeleteListener) {
        super(context, R.style.Dialog);
        this.mActivity = (Activity)context;
        this.taskPosition = taskPosition;
        this.context = context;
        this.taskBean = taskBean;
        this.taskDeleteListener = taskDeleteListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_task_detail);
        ButterKnife.bind(this, this);

        nameTv.setText(taskBean.getTitle());
        pathTv.setText(taskBean.getFolderPath());
        magnetTv.setText(taskBean.getMagnet());
        statusTv.setText("已完成");

        fileRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        fileAdapter = new BaseRvAdapter<DownloadedTaskBean.DownloadedTaskFileBean>(taskBean.getFileList()) {
            @NonNull
            @Override
            public AdapterItem<DownloadedTaskBean.DownloadedTaskFileBean> onCreateItem(int viewType) {
                return new TaskDownloadedFileItem(taskPosition, mActivity);
            }
        };
        fileRv.setAdapter(fileAdapter);
    }

    @OnClick({R.id.dialog_cancel_iv, R.id.path_tv, R.id.magnet_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dialog_cancel_iv:
                TaskDownloadedDetailDialog.this.dismiss();
                break;
            case R.id.path_tv:
                String path = taskBean.getFolderPath();
                ClipboardManager clipboardManagerPath = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataPath = ClipData.newPlainText("Label", path);
                if (clipboardManagerPath != null) {
                    clipboardManagerPath.setPrimaryClip(mClipDataPath);
                    ToastUtils.showShort("已复制路径：" + path);
                }
                break;
            case R.id.magnet_tv:
                String magnet = taskBean.getMagnet();
                ClipboardManager clipboardManagerMagnet = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataMagnet = ClipData.newPlainText("Label", magnet);
                if (clipboardManagerMagnet != null) {
                    clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                    ToastUtils.showShort("已复制Magnet：" + magnet);
                }
                break;
            case R.id.delete_tv:
                new CommonDialog.Builder(context)
                        .setAutoDismiss()
                        .showExtra()
                        .setOkListener(dialog ->{
                            deleteTaskFormDataBase(taskBean.get_id());
                            TaskDownloadedDetailDialog.this.dismiss();
                            taskDeleteListener.onTaskDelete(taskPosition);
                         })
                        .setExtraListener(dialog -> {
                            for (DownloadedTaskBean.DownloadedTaskFileBean fileBean : taskBean.getFileList()){
                                FileUtils.delete(fileBean.getFilePath());
                            }
                            deleteTaskFormDataBase(taskBean.get_id());
                            TaskDownloadedDetailDialog.this.dismiss();
                            taskDeleteListener.onTaskDelete(taskPosition);
                        })
                        .build()
                        .show( "确认删除任务？","删除任务和文件");
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDanmuBind(TaskBindDanmuEndEvent event) {
        if (TaskDownloadedDetailDialog.this.isShowing() && event.getTaskPosition() == taskPosition){
            int filePosition = event.getTaskFilePosition();
            if (filePosition > -1 && filePosition < taskBean.getFileList().size()){
                DownloadedTaskBean.DownloadedTaskFileBean fileBean = taskBean.getFileList().get(filePosition);
                fileBean.setDanmuPath(event.getDanmuPath());
                fileBean.setEpisode_id(event.getEpisodeId());

                DataBaseManager.getInstance()
                        .selectTable(15)
                        .update()
                        .where(2, fileBean.getFilePath())
                        .param(1, taskBean.get_id())
                        .param(4, event.getDanmuPath())
                        .param(5, event.getEpisodeId())
                        .postExecute();

                fileAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void deleteTaskFormDataBase(int taskId){
        DataBaseManager.getInstance()
                .selectTable(14)
                .delete()
                .where(0, String.valueOf(taskId))
                .execute();
        DataBaseManager.getInstance()
                .selectTable(15)
                .delete()
                .where(1, String.valueOf(taskId))
                .execute();

    }

    public interface TaskDeleteListener{
        void onTaskDelete(int position);
    }
}
