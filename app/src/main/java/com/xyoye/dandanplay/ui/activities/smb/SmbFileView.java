package com.xyoye.dandanplay.ui.activities.smb;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.libsmb.info.SmbFileInfo;

import java.util.List;

/**
 * Created by xyoye on 2020/1/3.
 */

interface SmbFileView extends BaseMvpView {

    void updateFileList(List<SmbFileInfo> smbFileInfoList);

    void updatePathText(String pathName);

    void setPreviousEnabled(boolean isEnabled);

    void launchPlayerActivity(String url, String zimu);
}
