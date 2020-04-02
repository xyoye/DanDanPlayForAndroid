package com.xyoye.dandanplay.ui.activities.smb;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.info.SmbFileInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/1/3.
 */

public class SmbFileActivity extends BaseMvpActivity<SmbFilePresenter> implements SmbFileView {

    @BindView(R.id.previous_iv)
    ImageView previousIv;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.smb_file_rv)
    RecyclerView smbFileRv;

    private List<SmbFileInfo> smbFileList;
    private BaseRvAdapter<SmbFileInfo> fileInfoAdapter;

    @NonNull
    @Override
    protected SmbFilePresenter initPresenter() {
        return new SmbFilePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_smb_file;
    }

    @Override
    public void initView() {
        setTitle("局域网 " + SmbManager.getInstance().getSmbType());

        smbFileList = new ArrayList<>();
        smbFileRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        fileInfoAdapter = new BaseRvAdapter<SmbFileInfo>(smbFileList) {
            @NonNull
            @Override
            public AdapterItem<SmbFileInfo> onCreateItem(int viewType) {
                return new SmbFileItem((fileName, isDir) -> {
                    if (isDir) {
                        presenter.openChildDirectory(fileName);
                    } else {
                        presenter.openFile(smbFileList, fileName);
                    }
                });
            }
        };
        smbFileRv.setAdapter(fileInfoAdapter);

        presenter.refreshSelfDirectory();
    }

    @Override
    public void initListener() {

    }

    @OnClick(R.id.path_rl)
    public void onViewClicked() {
        presenter.backParentDirectory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!SmbManager.getInstance().getController().isRootDir()) {
                presenter.backParentDirectory();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void updateFileList(List<SmbFileInfo> smbFileInfoList) {
        smbFileList.clear();
        smbFileList.addAll(smbFileInfoList);
        fileInfoAdapter.notifyDataSetChanged();
    }

    @Override
    public void updatePathText(String pathName) {
        pathTv.setText(pathName);
    }

    @Override
    public void setPreviousEnabled(boolean isEnabled) {
        previousIv.setImageResource(isEnabled ? R.drawable.ic_chevron_left_dark : R.drawable.ic_chevron_left_gray);
    }

    @Override
    public void launchPlayerActivity(String videoUrl, String zimu) {
        PlayerManagerActivity.launchPlayerSmb(
                SmbFileActivity.this,
                FileUtils.getFileNameNoExtension(videoUrl),
                videoUrl,
                zimu
        );
    }
}
