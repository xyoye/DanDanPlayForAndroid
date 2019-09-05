package com.xyoye.dandanplay.utils.jlibtorrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xyoye on 2019/9/5.
 */

public class TaskInfo {
    //任务列表
    public static List<TorrentTask> taskList = new ArrayList<>();
    //任务集合，key为hash，value为任务列表中序号
    public static Map<String, Integer> taskMap = new HashMap<>();

}
