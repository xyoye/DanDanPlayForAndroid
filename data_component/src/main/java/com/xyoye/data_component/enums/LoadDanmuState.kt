package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2022/1/2.
 */

enum class LoadDanmuState(val msg: String) {
    NOT_SUPPORTED("不支持自动匹配弹幕，请手动加载"),

    COLLECTING("正在收集自动匹配弹幕所需数据"),

    MATCHING("正在自动匹配弹幕"),

    NO_MATCHED("未匹配到弹幕，请手动加载"),

    MATCH_SUCCESS("匹配弹幕成功"),

    NO_MATCH_REQUIRE("无需匹配弹幕")
}