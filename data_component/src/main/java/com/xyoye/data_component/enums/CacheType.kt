package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2021/3/4.
 */

enum class CacheType(val dirName: String, val displayName: String, val clearTips: String) {
    DANMU_CACHE(
        "danmu",
        "弹幕文件",
        "弹幕缓存包括所有匹配、绑定、下载的弹幕，清除后将移除绑定并需要重新下载弹幕，确认清除？"
    ),
    SUBTITLE_CACHE(
        "subtitle",
        "字幕文件",
        "字幕缓存包括所有匹配、绑定、下载的字幕，清除后将移除绑定并需要重新下载字幕，确认清除？"
    ),
    PLAY_CACHE(
        "play_cache",
        "播放缓存",
        "播放缓存主要为播放网络视频的临时缓存，确认清除？"
    ),
    VIDEO_COVER_CACHE(
        "video_cover",
        "视频封面",
        "清除视频封面缓存，将在下一次播放后重新缓存视频封面，确认清除？"
    ),
    SCREEN_SHOT_CACHE(
        "screen_shot",
        "视频截图",
        "截图缓存为视频截图缓存，确认清除？"
    ),
    TORRENT_FILE_CACHE(
        "torrent",
        "种子文件",
        "清除种子文件缓存后，播放资源文件将重新下载种子文件，确认清除？"
    )
}