package com.xyoye.dandanplay.utils;

/**
 * Created by xyoye on 2019/8/23.
 */

public interface TaskManageListener {
    void pauseTask(String taskHash);

    void resumeTask(String taskHash);

    void deleteTask(String taskHash, boolean withFile);
}
