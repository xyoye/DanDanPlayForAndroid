package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

import java.util.List;

/**
 * Created by xyoye on 2019/6/26.
 */

public interface BlockManagerView extends BaseMvpView {

    List<String> updateData(List<String> blockData);
}
