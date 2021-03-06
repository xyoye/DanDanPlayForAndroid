package com.xyoye.common_component.storage.utils

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.IOUtils
import java.io.*

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

object DocumentUtils {
    private const val mAppDirName = "/DanDanPlay"
    private val mDocumentCache = LruCache<String, DocumentFile>(1000)

    fun getFile(context: Context, file: File, create: Boolean): DocumentFile? {
        //read form cache
        val cachedFile = mDocumentCache.get(file.absolutePath)
        if (cachedFile != null && cachedFile.isFile) {
            return cachedFile
        }

        if (file.parentFile == null) {
            return DocumentFile.fromFile(file)
        }

        val documentDirectory = getDocumentDirectory(context, file, create) ?: return null

        var documentFile = documentDirectory.findFile(file.name)
        if (documentFile != null) {
            if (!documentFile.isFile) {
                documentFile = null
            }
        } else {
            documentFile = if (create) {
                documentDirectory.createFile("application/octet-stream", file.name)
            } else {
                null
            }
        }

        if (documentFile != null) {
            mDocumentCache.put(file.absolutePath, documentFile)
        }

        return null
    }

    fun getDocumentFile(context: Context, file: File): DocumentFile? {
        try {
            //read form cache
            val cachedFile = mDocumentCache.get(file.absolutePath)
            if (cachedFile != null) {
                return cachedFile
            }

            //find root folder
            var baseFolder = ExSdCardUtils.getExtSdCardFolder(context, file)
                ?: return if (file.exists()) DocumentFile.fromFile(file) else null

            baseFolder = combineCachePath(baseFolder)

            //generate root uri
            val rootUri = getDocumentUri(context, File(baseFolder)) ?: return null
            var documentFile = DocumentFile.fromTreeUri(context, rootUri) ?: return null

            //split path
            val segments = if (baseFolder.length < file.absolutePath.length)
                file.absolutePath.substring(baseFolder.length + 1).split("/".toRegex())
            else
                arrayListOf()

            //step find document file
            for (segment in segments) {
                documentFile = documentFile.findFile(segment) ?: return null
            }

            //write file to cache
            mDocumentCache.put(file.absolutePath, documentFile)
            return documentFile
        } catch (t: Throwable) {
            DDLog.e("Error getting document file: $file", t)
            return null
        }
    }

    fun getDocumentDirectory(context: Context, file: File, create: Boolean = false): DocumentFile? {
        try {
            val cachedDirectory = mDocumentCache.get(file.absolutePath)
            if (cachedDirectory != null && cachedDirectory.isDirectory) {
                return cachedDirectory
            }

            var baseFolder = ExSdCardUtils.getExtSdCardFolder(context, file)
                ?: return if (create) {
                    if (file.mkdir()) DocumentFile.fromFile(file) else null
                } else {
                    if (file.isDirectory) DocumentFile.fromFile(file) else null
                }
            baseFolder = combineCachePath(baseFolder)

            //generate root uri
            val rootUri = getDocumentUri(context, File(baseFolder)) ?: return null
            var documentFile: DocumentFile =
                DocumentFile.fromTreeUri(context, rootUri) ?: return null

            //special DanDanPlay case
            if (create) {
                if (baseFolder.endsWith(mAppDirName) && !file.exists()) {
                    val newBaseFolder = baseFolder.substring(0, mAppDirName.length)
                    val newRootUri = getDocumentUri(context, File(newBaseFolder))
                    if (newRootUri != null) {
                        val rootDocumentFile = DocumentFile.fromTreeUri(context, rootUri)
                        val appDocumentFile = rootDocumentFile?.findFile(mAppDirName)
                        if (rootDocumentFile != null && appDocumentFile == null) {
                            // TODO: 2020/12/30 需要测试一下这里创建的文件夹路径是否正确
                            documentFile =
                                rootDocumentFile.createDirectory(mAppDirName) ?: return null
                        }
                    }
                }
            }

            //split path
            val segments = if (baseFolder.length < file.absolutePath.length)
                file.absolutePath.substring(baseFolder.length + 1).split("/".toRegex())
            else
                arrayListOf()

            for (segment in segments) {
                documentFile = documentFile.findFile(segment) ?: if (create) {
                    documentFile.createDirectory(segment) ?: return null
                } else {
                    return null
                }
            }

            mDocumentCache.put(file.absolutePath, documentFile)
            return documentFile
        } catch (t: Throwable) {
            DDLog.e("Error getting document directory: $file", t)
            return null
        }
    }

    fun getDocumentUri(context: Context, file: File): Uri? {
        val baseFolder = ExSdCardUtils.getExtSdCardFolder(context, file) ?: return null
        val volumeId = VolumeUtils.getVolumeId(context, baseFolder) ?: return null

        val relativePath =
            if (baseFolder.length < file.absolutePath.length)
                file.absolutePath.apply {
                    substring(baseFolder.length + 1)
                    replace("/", "%2F")
                    replace(" ", "%20")
                }
            else
                return null

        // TODO: 2020/12/30 严重怀疑漏了一个点
        val uri = "content://com.android.externalstorage.documents/tree/$volumeId%3A$relativePath"
        return Uri.parse(uri)
    }

    fun getDocumentPath(context: Context, treeUri: Uri, tree: Boolean = false): String? {
        val volumeId = getVolumeIdFromTreeUri(treeUri) ?: return null
        var volumePath = VolumeUtils.getVolumePath(context, volumeId) ?: return File.separator

        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length - 1)
        }

        var documentPath = getDocumentPathFormTreeUri(treeUri, tree) ?: volumePath
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length - 1)
        }

        if (documentPath.isNotEmpty()) {
            return if (documentPath.startsWith(File.separator))
                (volumePath + documentPath)
            else
                (volumePath + File.separator + documentPath)
        }

        return volumePath
    }

    fun copyDocumentFile(
        context: Context,
        srcDocumentFile: DocumentFile,
        destDocumentFile: DocumentFile
    ): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(srcDocumentFile.uri)
            outputStream = openOutputStream(context, destDocumentFile)
                ?: throw IOException("open document fie output stream failed: ${destDocumentFile.uri}")
            val buffer = ByteArray(16 * 1024) // MAGIC_NUMBER
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        } catch (e: Throwable) {
            DDLog.e(
                "Error when copying file from " + srcDocumentFile.uri + " to " + destDocumentFile.uri,
                e
            )
            return false
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }

        return true
    }

    fun writeData(context: Context, documentFile: DocumentFile, data: ByteArray): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = ByteArrayInputStream(data)
            outputStream = openOutputStream(context, documentFile)
                ?: throw IOException("open document fie output stream failed: ${documentFile.uri}")
            val buffer = ByteArray(16 * 1024) // MAGIC_NUMBER
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        } catch (e: Throwable) {
            DDLog.e("Error when writing bytes to ${documentFile.uri}", e)
            return false
        } finally {
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }

        return true
    }

    fun writeData(context: Context, documentFile: DocumentFile, inputStream: InputStream, notClose: Boolean): Boolean {
        var outputStream: OutputStream? = null
        try {
            outputStream = openOutputStream(context, documentFile)
                ?: throw IOException("open document fie output stream failed: ${documentFile.uri}")
            val buffer = ByteArray(16 * 1024) // MAGIC_NUMBER
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        } catch (e: Throwable) {
            DDLog.e("Error when writing bytes to ${documentFile.uri}", e)
            return false
        } finally {
            if (!notClose){
                IOUtils.closeIO(inputStream)
            }
            IOUtils.closeIO(outputStream)
        }

        return true
    }

    private fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val segments = getTreeDocumentId(treeUri)?.split(":".toRegex()) ?: return null
        return if (segments.isNotEmpty())
            segments[0]
        else
            null
    }

    private fun getDocumentPathFormTreeUri(treeUri: Uri, tree: Boolean): String? {
        val documentId =
            (if (tree) getTreeDocumentId(treeUri) else getDocumentId(treeUri)) ?: return null

        val segments = documentId.split(":".toRegex())
        if (segments.size >= 2) {
            return segments[1]
        }
        return null
    }

    private fun getTreeDocumentId(documentUri: Uri): String? {
        val paths = documentUri.pathSegments
        if (paths.size >= 2 && "tree" == paths[0]) {
            return paths[1]
        }
        return null
    }

    private fun getDocumentId(documentUri: Uri): String? {
        val paths = documentUri.pathSegments
        if (paths.size >= 4 && "document" == paths[2]) {
            return paths[3]
        }
        return null
    }

    private fun openOutputStream(context: Context, documentFile: DocumentFile): OutputStream? {
        var pfd = context.contentResolver.openFileDescriptor(documentFile.uri, "rw") ?: return null
        // this trick the internal system to trigger the media scanner on nothing
        val fd = pfd.detachFd()
        pfd = ParcelFileDescriptor.adoptFd(fd)
        return AutoSysOutputStream(pfd)
    }

    private fun combineCachePath(baseFolder: String): String {
        val cachePath = PathHelper.getCachePath()
        return if (cachePath.startsWith(baseFolder)) cachePath else baseFolder
    }
}