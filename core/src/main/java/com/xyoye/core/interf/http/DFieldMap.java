package com.xyoye.core.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by xyy on 2017/9/15
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DFieldMap {

    boolean encoded() default false;
}
