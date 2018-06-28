package com.xyoye.core.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by yzd on 2017/9/15 0015.
 *
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DField {

    String value();

    /**
     * Specifies whether the {@linkplain #value() name} and value are already URL encoded.
     */
    boolean encoded() default false;
}
