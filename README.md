# DanDanPlayForAndroid

![MIT License](https://img.shields.io/badge/licence-MIT-green.svg)

## 简介

弹弹play系列应用中安卓平台上的本地视频弹幕播放器，目前主要提供视频播放（本地+局域网）和弹幕加载（在线+本地）功能。
同时应用集成了弹弹提供的相关接口用于番剧下载、用户管理等。

## 下载

安卓平台可至[酷安](https://www.coolapk.com/apk/com.xyoye.dandanplay)下载。

其他平台可移步[弹弹play官网](http://www.dandanplay.com)下载。

## 功能

1、支持视频倍速播放

2、支持局域网播放

3、支持本地加载弹幕和网络查询加载弹幕

4、提供多种弹幕调整方式，如大小、速度、屏蔽、透明度等

5、提供字幕加载

6、提供音源切换

7、支持番剧下载

8、同步PC端追番、播放历史功能

## 学习使用
当你在编译使用此库时，会提示缺失KeyUtil.java，这个文件是保存了一些关键的密钥，由于一些原因不能公开，所以没有上传。
你需要，在util包下，新建KeyUtil.java，复制以下内容:


    import android.content.Context;
    
    public class KeyUtil {
    
      public static String getAppId(Context context){
        return "";
      }

      public static String getAppId2(Context context){
        return "";
      }

      public static String getAppSecret(Context context){
        return "";
      }

      public static String getUmengId(Context context){
        return "";
      }
    
    }

内容为空并不会影响播放相关功能，主要影响的是服务器接口方面的请求。

## 致谢
### 相关参与建设者：
概念版参与开发人员：xyoye、shine5402

弹弹接口提供者：kaedei

2.x版本开发者：shiwentao666

### 开源库
由于相关使用的开源库过多，这里仅列举主要功能实现库。
- DanmakuFlameMaster
- ijkplayer
- jcifs-ng
- jsoup
- ...
