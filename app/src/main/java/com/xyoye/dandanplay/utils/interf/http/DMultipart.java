package com.xyoye.dandanplay.utils.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Modified by xyoye on 2017/6/23.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface DMultipart {
}
