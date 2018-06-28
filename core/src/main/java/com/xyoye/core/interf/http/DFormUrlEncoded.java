package com.xyoye.core.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by yzd on 2017/9/18 0018.
 */

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface DFormUrlEncoded {
}
