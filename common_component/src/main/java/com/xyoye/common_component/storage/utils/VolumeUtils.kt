package com.xyoye.common_component.storage.utils

import android.content.Context
import android.os.storage.StorageManager
import com.xyoye.common_component.utils.DDLog
import java.lang.reflect.Array

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

object VolumeUtils {
    fun getVolumeId(context: Context, volumePath: String): String? {
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            val volumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getUUID = volumeClazz.getMethod("getUuid")
            val getPath = volumeClazz.getMethod("getPath")
            val result = getVolumeList.invoke(storageManager) ?: return null

            val length = Array.getLength(result)
            for (index in 0 until length) {
                val volumeElement = Array.get(result, index)
                val path = getPath.invoke(volumeElement) ?: continue

                if ((path as String) == volumePath) {
                    val uuid = getUUID.invoke(volumeElement)
                    if (uuid != null) {
                        return uuid as String
                    }
                }
            }
            return null
        } catch (t: Throwable){
            DDLog.e("Error in get volumeId: volumePath=$volumePath", t)
            return null
        }
    }

    fun getVolumePath(context: Context, volumeId: String): String?{
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            val volumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getUUID = volumeClazz.getMethod("getUuid")
            val getPath = volumeClazz.getMethod("getPath")
            val isPrimary = volumeClazz.getMethod("isPrimary")
            val result = getVolumeList.invoke(storageManager) ?: return null

            val length = Array.getLength(result)
            for (index in 0 until length) {
                val volumeElement = Array.get(result, index)
                val uuid = getUUID.invoke(volumeElement) ?: continue
                val primary = isPrimary.invoke(volumeElement) ?: continue

                if ((primary as Boolean) && "primary" == volumeId){
                    val path = getPath.invoke(volumeElement) ?: return null
                    return path as String
                }

                if ((uuid as String) == volumeId){
                    val path = getPath.invoke(volumeElement) ?: return null
                    return path as String
                }
            }
            return null
        } catch (t: Throwable){
            DDLog.e("Error in get path: volumeId=$volumeId", t)
            return null
        }
    }
}