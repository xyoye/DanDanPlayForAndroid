package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/3/30.
 */

public interface SmbPresenter extends BaseMvpPresenter {
    void querySqlDevice();

    void queryLanDevice();

    void addSqlDevice(SmbBean smbBean);

    void loginSmb(SmbBean smbBean, int position);

    void listSmbFolder(SmbBean smbBean);

    void openSmbFile(SmbBean smbBean);

    void returnParentFolder();

    void removeSqlDevice(String url);
}
