package com.xyoye.dandanplay.utils.scan.view;

import com.blankj.utilcode.util.LogUtils;

public class SimpleLog {

  private static boolean loggingEnabled = false;

  public static void setLoggingEnabled(boolean enabled) {
      loggingEnabled = enabled;
  }

  public static void d(String tag, String text) {
      if (loggingEnabled) {
          LogUtils.d(tag, text);
      }
  }

  public static void w(String tag, String text) {
      if (loggingEnabled) {
          LogUtils.w(tag, text);
      }
  }

  public static void w(String tag, String text, Throwable e) {
      if (loggingEnabled) {
          LogUtils.w(tag, text, e);
      }
  }

  public static void e(String tag, String text) {
      if (loggingEnabled) {
          LogUtils.e(tag, text);
      }
  }

  public static void d(String tag, String text, Throwable e) {
      if (loggingEnabled) {
          LogUtils.d(tag, text, e);
      }
  }

  public static void i(String tag, String text) {
      if (loggingEnabled) {
          LogUtils.i(tag, text);
      }
  }
}
