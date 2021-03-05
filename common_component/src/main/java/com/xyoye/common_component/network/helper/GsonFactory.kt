package com.xyoye.common_component.network.helper

import com.google.gson.Gson

/**
 * Created by xyoye on 2020/4/14.
 */

class GsonFactory private constructor() {
    companion object {
        val instance =
            Holder.instance
    }

    val mGson: Gson by lazy {
        Gson()
    }

    private object Holder {
        val instance =
            GsonFactory()
    }
}