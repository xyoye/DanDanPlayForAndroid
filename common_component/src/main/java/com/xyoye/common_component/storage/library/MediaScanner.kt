package com.xyoye.common_component.storage.library

import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Looper
import android.os.SystemClock
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

object MediaScanner {
    private const val SCAN_RETRIES = 6

    fun scanFiles(context: Context, paths: MutableList<String>) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            Librarian.getInstance().safePost(Runnable {
                scanFiles(context, paths)
            })
        }
        scanFiles(context, paths, SCAN_RETRIES)
    }

    private fun scanFiles(context: Context, paths: MutableList<String>, retries: Int) {
        if (paths.size == 0)
            return

        DDLog.i("scanFiles: About to scan files size: ${paths.size}, retries: $retries")

        val failedPaths = LinkedList<String>()
        val finishSignal = CountDownLatch(paths.size)

        MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { path, uri ->
            if (uri == null) {
                failedPaths.add(path)
                DDLog.i("scanFiles: Scan failed for path: $path, uri: $uri")
            } else {
                if (getSize(context, uri) == 0L){
                    DDLog.w("scanFiles: Scan failed, Scan returned an uri but stored size is 0, path: $path, uri:$uri")
                    failedPaths.add(path)
                }
            }
            finishSignal.countDown()
        }

        try {
            finishSignal.await(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException){
            // ignore
        }

        if (failedPaths.size > 0 && retries > 0){
            // didn't want to do this, but there is a serious timing issue with the SD
            // and storage in general
            SystemClock.sleep(2000)
            scanFiles(context, failedPaths, retries - 1)
        }
    }

    private fun getSize(context: Context, uri: Uri): Long {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, arrayOf("_size"), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        } catch (e: Throwable) {
            DDLog.e("Error getting file size for uri: $uri", e)
        } finally {
            IOUtils.closeIO(cursor)
        }
        return 0
    }
}