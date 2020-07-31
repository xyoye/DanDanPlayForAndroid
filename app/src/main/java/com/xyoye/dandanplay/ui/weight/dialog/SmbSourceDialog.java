package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.FileManagerBean;
import com.xyoye.dandanplay.ui.weight.item.SmbFileManagerItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.smb.SmbManager;
import com.xyoye.smb.info.SmbFileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/6/17.
 */

public class SmbSourceDialog extends Dialog {
    public final static int SOURCE_DANMU = 10001;
    public final static int SOURCE_ZIMU = 10002;

    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.file_rv)
    RecyclerView fileRv;

    private int sourceType;
    private Context mContext;
    private Dialog parentDialog;
    private OnSelectedListener listener;

    private List<FileManagerBean> showFileList;
    private BaseRvAdapter<FileManagerBean> adapter;

    public SmbSourceDialog(@NonNull Context context, int sourceType, Dialog parentDialog, OnSelectedListener listener) {
        super(context, R.style.Dialog);
        this.mContext = context;
        this.sourceType = sourceType;
        this.parentDialog = parentDialog;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_smb_source);
        ButterKnife.bind(this);

        titleTv.setText(sourceType == SOURCE_DANMU
                ? "选择局域网弹幕"
                : "选择局域网字幕");

        String currentPath = SmbManager.getInstance().getController().getCurrentPath();
        pathTv.setText(currentPath);

        showFileList = new ArrayList<>();
        adapter = new BaseRvAdapter<FileManagerBean>(showFileList) {
            @NonNull
            @Override
            public AdapterItem<FileManagerBean> onCreateItem(int viewType) {
                return new SmbFileManagerItem((path, isFolder) -> downloadResource(path));
            }
        };

        fileRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        fileRv.setNestedScrollingEnabled(false);
        fileRv.setItemViewCacheSize(10);
        fileRv.setAdapter(adapter);

        querySmbResourceList();
    }

    @OnClick(R.id.cancel_tv)
    public void onViewClicked() {
        SmbSourceDialog.this.dismiss();
    }

    private void querySmbResourceList(){
        IApplication.getExecutor().execute(() -> {
            List<SmbFileInfo> smbFileList = SmbManager.getInstance().getController().getSelfList();
            for (SmbFileInfo fileInfo : smbFileList) {
                LogUtils.e(fileInfo.getFileName());
                if (fileInfo.isDirectory())
                    continue;
                if (sourceType == SOURCE_ZIMU) {
                    String ext = FileUtils.getFileExtension(fileInfo.getFileName());
                    switch (ext.toUpperCase()) {
                        case "ASS":
                        case "SCC":
                        case "STL":
                        case "SRT":
                        case "TTML":
                            FileManagerBean managerBean = new FileManagerBean();
                            managerBean.setFolder(false);
                            managerBean.setName(fileInfo.getFileName());
                            IApplication.getMainHandler().post(() -> adapter.addItem(managerBean));
                            break;
                    }
                } else if (sourceType == SOURCE_DANMU) {
                    String ext = FileUtils.getFileExtension(fileInfo.getFileName());
                    if ("XML".equals(ext.toUpperCase())) {
                        FileManagerBean managerBean = new FileManagerBean();
                        managerBean.setFolder(false);
                        managerBean.setName(fileInfo.getFileName());
                        IApplication.getMainHandler().post(() -> adapter.addItem(managerBean));
                    }
                }
            }

        });

    }

    private void downloadResource(String fileName) {
        IApplication.getExecutor().execute(() -> {
            FileOutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                File resourceFile = getSaveResourceFile(fileName);
                outputStream = new FileOutputStream(resourceFile);
                inputStream = SmbManager.getInstance().getController().getFileInputStream(fileName);
                long contentLength = SmbManager.getInstance().getController().getFileLength(fileName);
                if (inputStream != null) {
                    readAndWriteFile(inputStream, outputStream, contentLength);
                    IApplication.getMainHandler().post(() -> {
                        if (listener != null){
                            listener.onSelected(resourceFile.getAbsolutePath());
                        }
                        if (parentDialog != null && parentDialog.isShowing()){
                            parentDialog.dismiss();
                            SmbSourceDialog.this.dismiss();
                        }
                    });
                } else {
                    ToastUtils.showShort("获取资源失败，无法绑定");
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                CommonUtils.closeResource(outputStream);
                CommonUtils.closeResource(inputStream);
            }
        });
    }

    private File getSaveResourceFile(String fileName) throws IOException {
        String directoryName = sourceType == SOURCE_ZIMU
                ? Constants.DefaultConfig.subtitleFolder
                : Constants.DefaultConfig.danmuFolder;
        String folderPath = Constants.DefaultConfig.downloadPath + directoryName;
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        File resourceFile = new File(folder, fileName);
        if (resourceFile.exists()) {
            resourceFile.delete();
        }
        resourceFile.createNewFile();
        return resourceFile;
    }

    private void readAndWriteFile(InputStream inputStream, OutputStream outputStream, long contentLength) throws IOException{
        int bufferSize = 512 * 1024;
        long readTotalSize = 0;
        byte[] readBuffer = new byte[bufferSize];
        long readSize = Math.min(bufferSize, contentLength);
        int readLen = inputStream.read(readBuffer, 0, (int) readSize);

        while (readLen > 0 && readTotalSize < contentLength) {
            outputStream.write(readBuffer, 0, readLen);
            readTotalSize += readLen;
            readSize = (bufferSize > (contentLength - readTotalSize))
                    ? (contentLength - readTotalSize)
                    : bufferSize;
            readLen = inputStream.read(readBuffer, 0, (int) readSize);
        }
        outputStream.flush();
    }

    public interface OnSelectedListener {
        void onSelected(String path);
    }
}
