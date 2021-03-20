# [DanDanPlayForAndroid](https://github.com/xyoye/DanDanPlayForAndroid)

## 简介

《弹弹play 概念版》是一个本地视频播放器，是弹弹play系列应用安卓平台的实现，致力于视频+弹幕的播放，为您带来愉悦的观影体验。

## 下载

安卓平台: 1.[历史版本](https://github.com/xyoye/DanDanPlayForAndroid/releases)，2.[酷安应用市场](https://www.coolapk.com/apk/com.xyoye.dandanplay)

其他平台: [弹弹play官网](http://www.dandanplay.com)

# 一、 应用介绍

## 功能介绍

- 视频
  - 提供双内核（IJK、EXO）切换，适配常见视频格式
  - 支持局域网文件浏览播放
  - 支持FTP文件浏览播放
  - 支持WebDav文件浏览播放
- 弹幕
  - 支持根据视频自动匹配弹幕
  - 支持弹幕搜索、下载
  - 支持弹幕样式调整，大小、速度、描边、透明度等
  - 支持关键字屏蔽、正则表达式屏蔽
- 字幕
  - 支持根据视频自动匹配字幕
  - 支持字幕搜索、下载
  - 支持字幕样式调整，大小、描边、颜色等
  - 支持外挂字幕
- 动漫资讯
  - 提供每周番剧，动漫更新不错过
  - 提供番剧搜索，你想要的都能找到
  - 提供番剧详情，番剧信息一网打尽

## 应用截图

<div>
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/storage_list.jpg" width="200px">
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/video_list.jpg" width="200px">
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/search.jpg" width="200px">
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/anime_detail.jpg" width="200px">
</div>

<div>
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/video_1.jpg" width="400px"/>
	<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/video_2.jpg" width="400px"/>
</div>


# 二、项目介绍

4.x版本（当前）使用Kotlin + MVVM + 组件化方案实现

3.x版本使用Java + MVP模式开发，详细信息请切换分支查看

本项目接口由[弹弹play开放平台](https://api.acplay.net/swagger/ui/index)提供

## 项目结构

<img src="https://github.com/xyoye/ImageRepository/blob/master/DanDanPlayer/4.0/module.png" width="800p"/>

## 模块介绍

|  模块   | 说明  |
|  ----  | ----  |
| APP  | 项目入口，包含启动页及主框架 |
| Anime  | 动画模块，首页、搜索、季番、番剧详情等 |
| Download  | 下载模块，包括Torrent下载（未完成）、磁链解析 |
| Stream  | 网络数据模块，包含SMB、FTP、WebDav、串流等 |
| Local  | 本地数据模块，包含本地视频、弹幕下载、字幕下载 |
| User  | 用户模块，包含用户信息、登录注册、应用设置等 |
| Player  | 播放器模块 |
| Common  | 基础模块，包括基类、通用组件、工具类等 |
| Data  | 数据模块，包含普通Bean类、数据库Entity类、枚举类等 |

注：其中User模块出于安全考虑，已关闭用户相关接口的调用，编译安装后将无法使用相关功能

## 项目配置

1.日志开关，根目录下gradle.properties文件，配置IS_DEBUG_MODE，修改后rebuild project

2.单独编译模块，根目录下gradle.properties文件，配置IS_APPLICATION_RUN，设置true代表模块以应用类型编译，修改后rebuild project

## 自定义工具说明

### 1. MVVM插件

plugin目录下有MVVMTemplate-xx.jar，此插件用于快速生成符合项目的MVVM文件（Activity/Fragment、ViewModel、layout），可通过Android Studio安装此插件。使用及更多说明见[MVVMTemplate](https://github.com/xyoye/MVVMTemplate)项目。

### 2. MMKV注解

项目中使用MMKV实现key-value
数据存储，通过自定义注解的方式，实现了快速生成MMKV调用方法，使用实例见common模块下config目录，关于注解的更多说明见[MMKVStorage](https://github.com/xyoye/MMKVStorage)项目。
