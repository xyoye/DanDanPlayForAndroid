package com.xyoye.core.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by yzd on 2017/10/12 0012.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface DPath {

    String value();

    /**
     * Specifies whether the argument value to the annotated method parameter is already URL encoded.
     */
    boolean encoded() default false;
}
