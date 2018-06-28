package com.xyoye.core.interf.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by yzd on 2017/10/14 0014.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DPart {
    /**
     * The name of the part. Required for all parameter types except
     * {@link okhttp3.MultipartBody.Part}.
     */
    String value() default "";
    /** The {@code Content-Transfer-Encoding} of this part. */
    String encoding() default "binary";
}
