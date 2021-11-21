package com.xyoye.common_component.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.xyoye.common_component.R
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.data_component.entity.VideoEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by xyoye on 2020/11/26.
 */

private val commonVideoExtension = arrayOf(
    "3gp", "avi", "flv", "mp4",
    "m4v", "mkv", "mov", "mpeg",
    "mpg", "mpe", "rm", "rmvb",
    "wmv", "asf", "asx", "dat",
    "vob", "m3u8"
)

val supportSubtitleExtension = arrayOf(
    "ass", "scc", "stl", "srt",
    "ttml"
)

fun isVideoFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return commonVideoExtension.contains(extension.toLowerCase(Locale.ROOT))
}

fun isSubtitleFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return supportSubtitleExtension.contains(extension.toLowerCase(Locale.ROOT))
}

fun isDanmuFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return "xml" == extension.toLowerCase(Locale.ROOT)
}

fun isTorrentFile(filePath: String): Boolean {
    val extension = getFileExtension(filePath)
    return extension.toLowerCase(Locale.ROOT) == "torrent"
}

object MediaUtils {

    /**
     * 获取文件类型对应图标
     */
    fun getMediaTypeCover(filePath: String): Int {
        return when {
            isVideoFile(filePath) -> R.drawable.ic_file_video
            isSubtitleFile(filePath) -> R.drawable.ic_file_subtitle
            isDanmuFile(filePath) -> R.drawable.ic_file_xml
            isTorrentFile(filePath) -> R.drawable.ic_file_torrent
            else -> R.drawable.ic_file_unknow
        }
    }

    /**
     * 保存视频截图
     *
     * pair.first : 是否保存成功
     * pair.second: 保存目录类型，私有目录 or 公共目录 or ""
     */
    fun saveScreenShot(context: Context, bitmap: Bitmap): Pair<Boolean, String> {
        //尝试保存文件到公共目录：Picture
        val resolver = context.contentResolver
        val pictureDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, getShotImageName())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val pictureUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            pictureDetails
        ) ?: return Pair(first = false, second = "")

        if (saveImage(resolver, pictureUri, bitmap)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pictureDetails.clear()
                pictureDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(pictureUri, pictureDetails, null, null)
            }
            return Pair(first = true, second = "公共目录")
        }

        //保存到公共录失败，尝试保存到私有目录
        val pictureFile = File(PathHelper.PATH_SCREEN_SHOT, getShotImageName())
        return if (saveImage(pictureFile, bitmap)) {
            Pair(first = true, second = "私有目录")
        } else {
            Pair(first = false, second = "")
        }
    }

    /**
     * 扫描文件夹内视频文件
     */
    fun scanVideoFile(folderPath: String): MutableList<VideoEntity> {
        val folderFile = File(folderPath)
        if (!folderFile.exists())
            return mutableListOf()

        val childFileArray = folderFile.listFiles() ?: return mutableListOf()

        val videoEntities = mutableListOf<VideoEntity>()
        childFileArray.forEach {
            if (it.isFile && isVideoFile(it.absolutePath)) {
                videoEntities.add(
                    VideoEntity(
                        0,
                        0,
                        0,
                        it.absolutePath,
                        folderPath,
                        null,
                        null,
                        0,
                        it.length(),
                        isFilter = false,
                        isExtend = true
                    )
                )
            } else if (it.isDirectory) {
                videoEntities.addAll(scanVideoFile(it.absolutePath))
            }
        }
        return videoEntities
    }

    /**
     * 获取Uri对应文件的真实路径
     */
    fun getPathFromURI(contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = BaseApplication.getAppContext().contentResolver.query(
                contentUri,
                proj,
                null,
                null,
                null
            )
            if (cursor == null || cursor.count == 0)
                return ""
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return Uri.fromFile(File(cursor.getString(columnIndex))).toString()
        } catch (e: IllegalArgumentException) {
            return ""
        } catch (e: SecurityException) {
            return ""
        } catch (e: SQLiteException) {
            return ""
        } catch (e: NullPointerException) {
            return ""
        } finally {
            if (cursor != null && !cursor.isClosed) cursor.close()
        }
    }

    private fun getShotImageName(): String {
        val currentTimeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val curTime: String = currentTimeFormat.format(Date())
        return "SHOT_$curTime.jpg"
    }

    private fun saveImage(resolver: ContentResolver, pictureUri: Uri, bitmap: Bitmap): Boolean {
        val fileDescriptor = resolver.openFileDescriptor(pictureUri, "w", null) ?: return false
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(fileDescriptor.fileDescriptor)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeIO(fileOutputStream)
            IOUtils.closeIO(fileDescriptor)
        }
        return false
    }

    private fun saveImage(file: File, bitmap: Bitmap): Boolean {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeIO(fileOutputStream)
        }
        return false
    }
}