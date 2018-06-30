package com.xyoye.dandanplay.ui.temp;

import com.google.gson.Gson;

/**
 * Title: GsonManager <br>
 * Description: <br>
 * Copyright (c) 传化物流版权所有 2016 <br>
 * Created DateTime: 2016/12/2 10:03
 * Created by Wentao.Shi.
 */
public class GsonManager {
    private static Gson sGson;
    public static Gson getInstance(){
        if (sGson==null)
            sGson=new Gson();
        return sGson;
    }
}
