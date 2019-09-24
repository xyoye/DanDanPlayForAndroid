package com.xyoye.dandanplay.ui.weight.dialog;

import android.annotation.SuppressLint;
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

import com.blankj.utilcode.util.ToastUtils;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.Priority;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.ui.weight.item.TaskDownloadingFileItem;
import com.xyoye.dandanplay.utils.TaskManageListener;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.TaskStateBean;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentChildFile;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngine;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentTask;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadingDetailDialog extends Dialog {
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

    private String statusStr;
    private TaskStateBean taskStateBean;
    private TaskManageListener taskManageListener;

    public TaskDownloadingDetailDialog(@NonNull Context context, TaskStateBean taskStateBean, String statusStr, TaskManageListener taskManageListener) {
        super(context, R.style.Dialog);
        this.context = context;
        this.statusStr = statusStr;
        this.taskStateBean = taskStateBean;
        this.taskManageListener = taskManageListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_downloading_task_detail);
        ButterKnife.bind(this, this);

        nameTv.setText(taskStateBean.getTaskName());
        pathTv.setText(taskStateBean.getSaveDirPath());
        magnetTv.setText(TorrentUtil.MAGNET_HEADER + taskStateBean.getTorrentHash());
        statusTv.setText(statusStr);

        fileRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        BaseRvAdapter<TorrentChildFile> fileAdapter = new BaseRvAdapter<TorrentChildFile>(getChildFileList()) {
            @NonNull
            @Override
            public AdapterItem<TorrentChildFile> onCreateItem(int viewType) {
                return new TaskDownloadingFileItem();
            }
        };
        fileRv.setAdapter(fileAdapter);
    }

    @OnClick({R.id.dialog_cancel_iv, R.id.path_tv, R.id.magnet_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dialog_cancel_iv:
                TaskDownloadingDetailDialog.this.dismiss();
                break;
            case R.id.path_tv:
                String path = pathTv.getText().toString();
                ClipboardManager clipboardManagerPath = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataPath = ClipData.newPlainText("Label", path);
                if (clipboardManagerPath != null) {
                    clipboardManagerPath.setPrimaryClip(mClipDataPath);
                    ToastUtils.showShort("已复制路径：" + path);
                }
                break;
            case R.id.magnet_tv:
                String magnet = magnetTv.getText().toString();
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
                        .setOkListener(dialog -> {
                            TaskDownloadingDetailDialog.this.dismiss();
                            taskManageListener.deleteTask(taskStateBean.getTorrentHash(), false);
                        })
                        .setExtraListener(dialog -> {
                            TaskDownloadingDetailDialog.this.dismiss();
                            taskManageListener.deleteTask(taskStateBean.getTorrentHash(), true);
                        })
                        .build()
                        .show("确认删除任务？", "删除任务和文件");
                break;
        }

    }

    private List<TorrentChildFile> getChildFileList() {
        String torrentHash = taskStateBean.getTorrentHash();
        TorrentTask torrentTask = TorrentEngine.getInstance().getTorrentTask(torrentHash);
        if (torrentTask == null)
            return new ArrayList<>();
        FileStorage fileStorage = torrentTask.getTorrentFiles();
        long[] receivedBytes = torrentTask.getFilesReceivedBytes();
        Priority[] priorities = torrentTask.getTorrent().getPriorities();

        List<TorrentChildFile> childFileList = new ArrayList<>();
        for (int i = 0; i < fileStorage.numFiles(); i++) {
            TorrentChildFile childFile = new TorrentChildFile(
                    fileStorage.fileName(i),
                    fileStorage.fileSize(i),
                    receivedBytes[i],
                    priorities[i] != Priority.IGNORE
            );
            childFileList.add(childFile);
        }
        return childFileList;
    }
}
