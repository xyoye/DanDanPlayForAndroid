package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import java.util.List;

/**
 * Created by YE on 2018/10/27.
 */


public interface DownloadManagerView extends BaseMvpView, LoadDataView {
    void refreshAdapter(List<Torrent> torrentList);

    void startNewTask();
}
