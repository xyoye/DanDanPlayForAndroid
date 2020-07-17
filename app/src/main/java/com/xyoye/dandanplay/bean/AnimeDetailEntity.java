package com.xyoye.dandanplay.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by xyoye on 2020/7/16.
 */

public class AnimeDetailEntity implements MultiItemEntity {
    //剧集
    public static final int TYPE_EPISODE = 101;
    //相关推荐
    public static final int TYPE_RECOMMEND = 102;
    //更多推荐
    public static final int TYPE_MORE = 103;

    private int type;
    private Object object;

    public AnimeDetailEntity(int type, Object object) {
        this.type = type;
        this.object = object;
    }

    public int getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
