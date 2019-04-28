package com.player.ijkplayer.danmaku;

import java.util.List;

/**
 * Created by long on 2016/12/22.
 * 弹幕监听器
 */
public interface OnDanmakuListener<T> {

    /**
     * 这个用来监听控制全屏模式下的发送弹幕操作，会在进入弹幕之前调用，并且根据返回值来判断是否可以进行发射弹幕操作，可用来处理用户登录情况
     * @return true：进入弹幕编辑；false：当前不能发射弹幕
     */
    boolean isValid();

    /**
     * 获取发射的弹幕数据
     * @param data  弹幕数据，默认为{@link com.player.danmaku.danmaku.model.BaseDanmaku}，如果设置自定义类型则为自定义格式
     */
    void onDataObtain(T data);

    /**
     * 开启 or 关闭云屏蔽
     */
    void setCloudFilter(boolean isOpen);

    /**
     * 删除一个屏蔽
     */
    void deleteBlock(String text);

    /**
     * 增加一个屏蔽
     */
    void addBlock(String text);

    /**
     * 查询所有屏蔽
     */
    List<String> queryBlock();
}
