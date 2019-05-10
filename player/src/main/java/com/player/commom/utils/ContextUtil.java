package com.player.commom.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Created by xyy on 2018/12/20.
 */

public class ContextUtil {
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private static ContextUtil contextUtil;

    private ContextUtil(){

    }

    public void initContext(Context context){
        if (this.context == null)
            this.context = context;
    }

    public static ContextUtil getInstans(){
        if (contextUtil == null){
            contextUtil = new ContextUtil();
        }
        return contextUtil;
    }

    public Context getContext() {
        if (context == null)
            throw new NullPointerException("ContextUtil must init");
        return context;
    }
}
