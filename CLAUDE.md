# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

弹弹play 概念版 (DanDanPlay) — Android 本地视频播放器，支持弹幕（danmaku）叠加播放。4.x 版本使用 Kotlin + MVVM + 组件化架构。

## 构建命令

```bash
# 前置条件：JDK 11，Android SDK API 33
./gradlew clean assembleDebug      # Debug 构建（使用内置 debug.jks）
./gradlew clean assembleBeta       # Beta 构建
./gradlew clean assembleRelease    # Release 构建（需要签名环境变量）
./gradlew installDebug             # 安装到设备
./gradlew dependencyUpdates        # 依赖更新报告
```

Release 构建需要环境变量：`KEYSTORE_PASS`、`ALIAS_NAME`、`ALIAS_PASS`。

## Gradle Properties 配置

在 `gradle.properties` 中设置：
- `IS_DEBUG_MODE` — 日志开关，修改后需 rebuild
- `IS_APPLICATION_RUN=true` — 将模块以独立应用方式编译

## 架构概览

### 组件化模块结构

```
app/                    -- 应用入口、启动页、MainActivity
anime_component/        -- 动画模块：首页、搜索、番剧详情
user_component/         -- 用户模块：登录、设置（出于安全考虑已关闭接口调用）
local_component/        -- 本地数据：视频列表、弹幕/字幕下载
storage_component/      -- 网络浏览：SMB、FTP、WebDAV
player_component/       -- 播放器：双内核（IJK/EXO）、VLC
common_component/       -- 基础模块：基类、网络、数据库、工具类
data_component/         -- 数据模型：Bean、Room Entity、枚举、Moshi
buildSrc/               -- 构建配置：Versions、Dependencies、插件
repository/             -- 预编译 AAR：danmaku、immersion_bar、panel_switch 等
```

所有功能模块通过 `common_component` 共享基础能力，模块间使用 ARouter 路由导航（路由表见 `RouteTable.kt`）。

### MVVM 基类

每个页面遵循 Activity/Fragment + ViewModel 配对模式：
- `BaseActivity<VM, V>` / `BaseFragment<VM, V>` — 泛型绑定 ViewModel 和 DataBinding
- `BaseViewModel` — 提供 LiveData 加载状态管理
- 使用 `ViewModelInit` 通过 BR variable ID 绑定

### 数据层

- **网络：** Retrofit 2.9 + Moshi，6 个 Service 接口（`DanDanService`、`ExtendedService`、`RemoteService`、`MagnetService`、`ScreencastService`、`AlistService`），Repository 模式
- **数据库：** Room 2.4.3，数据库版本 13，8 个 Entity，8 个 DAO，12 个手动 Migration
- **键值存储：** MMKV + 自定义注解处理器，配置类在 `common_component/config/`

### 播放器

支持三种引擎切换：IJK（默认）、ExoPlayer 2.18、LibVLC 4.0。弹幕渲染使用 DanmakuFlameMaster。

## 关键文件

| 用途 | 路径 |
|---|---|
| 模块声明 | `settings.gradle.kts` |
| SDK/版本常量 | `buildSrc/src/main/java/Versions.kt` |
| 依赖坐标 | `buildSrc/src/main/java/Dependencies.kt` |
| 应用构建配置 | `buildSrc/src/main/java/setup/Application.kt` |
| 模块构建配置 | `buildSrc/src/main/java/setup/Module.kt` |
| MVVM 基类 | `common_component/src/main/java/com/xyoye/common_component/base/` |
| 网络层 | `common_component/src/main/java/com/xyoye/common_component/network/Retrofit.kt` |
| 数据库管理 | `common_component/src/main/java/com/xyoye/common_component/database/DatabaseManager.kt` |
| 路由表 | `common_component/src/main/java/com/xyoye/common_component/config/RouteTable.kt` |
| Application | `app/src/main/java/com/xyoye/dandanplay/app/IApplication.kt` |

## 代码风格

使用 Kotlin 官方代码风格（`kotlin.code.style=official`），无额外 linter 配置。项目未配置单元测试体系。

## 自定义工具

- `other/` 目录下有 MVVM 模板插件 JAR，用于生成符合项目规范的 Activity/Fragment + ViewModel + Layout 文件
- MMKV 注解处理器自动生成键值存储调用方法，用法见 `common_component/config/` 目录
