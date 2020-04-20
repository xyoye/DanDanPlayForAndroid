package com.xyoye.dandanplay.ui.activities.smb;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;
import com.xyoye.smb.info.SmbFileInfo;

import java.util.List;

/**
 * Created by xyoye on 2020/1/3.
 */

interface SmbFilePresenter extends BaseMvpPresenter {

    void refreshSelfDirectory();

    void backParentDirectory();

    void openChildDirectory(String dirName);

    void openFile(List<SmbFileInfo> smbFileInfoList, String fileName);
}