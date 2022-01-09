package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2022/1/2.
 */

enum class LoadDanmuState(val msg: String) {
    NOT_SUPPORTED("不支持"),

    COLLECTING("数据收集中"),

    MATCHING("正在匹配"),

    NO_MATCHED("无相关弹幕"),

    MATCH_SUCCESS("匹配弹幕成功")
}