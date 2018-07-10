package com.xyoye.core.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by yzd on 2017/7/6 0006.
 */

public class GsonFactory {

    private static Gson mGson;

    public static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Integer.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(int.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(Double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(Long.class, new LongDefaultAdapter())
                .registerTypeAdapter(long.class, new LongDefaultAdapter())
                .registerTypeAdapter(Boolean.class, new BooleanDefaultAdapter())
                .registerTypeAdapter(boolean.class, new BooleanDefaultAdapter())
                .create();
    }

    public static Gson getGson() {
        if (mGson == null) {
            mGson = buildGson();
        }
        return mGson;
    }
}
