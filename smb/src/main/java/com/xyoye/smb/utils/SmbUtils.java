package com.xyoye.smb.utils;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbUtils {

    public static boolean isTextEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean containsEmptyText(String... strings) {
        for (String string : strings) {
            if (isTextEmpty(string))
                return true;
        }
        return false;
    }
}
