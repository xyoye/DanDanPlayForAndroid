package com.xyoye.dandanplay.utils;

import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/9/19.
 */

public class DanmuFilterUtils {
    private List<String> localFilterList;
    private List<String> cloudFilterList;

    private DanmuFilterUtils() {
        cloudFilterList = new ArrayList<>();
        localFilterList = new ArrayList<>();
    }

    private static class Holder {
        static DanmuFilterUtils instance = new DanmuFilterUtils();
    }

    public static DanmuFilterUtils getInstance() {
        return Holder.instance;
    }

    /**
     * 添加一个屏蔽关键词
     */
    public void addLocalFilter(String text){
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .insert()
                .param("text", text)
                .postExecute();
        localFilterList.add(text);
    }

    /**
     * 移除一个屏蔽关键词
     */
    public void removeLocalFilter(String text){
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .delete()
                .where("text", text)
                .postExecute();
        localFilterList.remove(text);
    }

    /**
     * 清空屏蔽关键词
     */
    public void clearLocalFilter(){
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .delete()
                .postExecute();
        localFilterList.clear();
    }

    /**
     * 更新本地屏蔽关键词
     */
    public void updateLocalFilter() {
        DataBaseManager.getInstance()
                .selectTable("danmu_block")
                .query()
                .queryColumns("text")
                .postExecute(cursor -> {
                    while (cursor.moveToNext()) {
                        localFilterList.add(cursor.getString(0));
                    }
                });
    }

    /**
     * 更新云屏蔽关键词
     */
    public void updateCloudFilter() {
        long lastUpdateTime = AppConfig.getInstance().getUpdateFilterTime();
        long nowTime = System.currentTimeMillis();

        //七天更新一次云过滤列表
        if (nowTime - lastUpdateTime > 7 * 24 * 60 * 60 * 1000) {
            AppConfig.getInstance().setUpdateFilterTime(nowTime);
            IApplication.getSqlThreadPool().execute(() -> {
                List<String> filters = DanmuUtils.getFilterString();
                cloudFilterList.clear();
                cloudFilterList.addAll(filters);

                DataBaseManager.getInstance()
                        .selectTable("cloud_filter")
                        .delete()
                        .executeAsync();
                for (int i = 0; i < filters.size(); i++) {
                    DataBaseManager.getInstance()
                            .selectTable("cloud_filter")
                            .insert()
                            .param("filter", filters.get(i))
                            .executeAsync();
                }
            });
        } else {
            this.cloudFilterList.clear();
            DataBaseManager.getInstance()
                    .selectTable("cloud_filter")
                    .query()
                    .queryColumns("filter")
                    .postExecute(cursor -> {
                        while (cursor.moveToNext()) {
                            String text = cursor.getString(0);
                            this.cloudFilterList.add(text);
                        }
                    });
        }

    }

    public List<String> getLocalFilter() {
        return localFilterList;
    }

    public List<String> getCloudFilter() {
        return cloudFilterList;
    }
}
