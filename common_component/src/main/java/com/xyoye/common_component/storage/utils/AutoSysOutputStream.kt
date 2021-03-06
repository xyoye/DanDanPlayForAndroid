package com.xyoye.common_component.storage.utils

import android.os.ParcelFileDescriptor

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

class AutoSysOutputStream(fd: ParcelFileDescriptor)  : ParcelFileDescriptor.AutoCloseOutputStream(fd){

    override fun close() {
        sync()
        super.close()
    }

    private fun sync(){
        try {
            fd.sync()
        } catch (t: Throwable){
            // ignore
        }
    }
}