package com.xyoye.common_component.storage.file_system

import android.content.Context
import com.xyoye.common_component.storage.library.Librarian
import com.xyoye.common_component.storage.library.MediaScanner
import com.xyoye.common_component.storage.utils.DocumentUtils
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * @author gubatron
 * @author aldenml
 *
 * Created by xyoye on 2020/12/30.
 */

class LollipopFileSystem(private val context: Context) : AbstractFileSystem() {

    override fun isDirectory(file: File): Boolean {
        if (super.isDirectory(file))
            return true
        return DocumentUtils.getDocumentFile(context, file)?.isDirectory ?: false
    }

    override fun isFile(file: File): Boolean {
        if (super.isFile(file))
            return true
        return DocumentUtils.getDocumentFile(context, file)?.isFile ?: false
    }

    override fun canWrite(file: File): Boolean {
        if (super.canWrite(file))
            return true
        return DocumentUtils.getDocumentFile(context, file)?.canWrite() ?: false
    }

    override fun length(file: File): Long {
        val length = super.length(file)
        if (length > 0)
            return length
        return DocumentUtils.getDocumentFile(context, file)?.length() ?: 0L
    }

    override fun lastModified(file: File): Long {
        val date = super.lastModified(file)
        if (date > 0)
            return date
        return DocumentUtils.getDocumentFile(context, file)?.lastModified() ?: 0L
    }

    override fun exists(file: File): Boolean {
        if (super.exists(file))
            return true
        return DocumentUtils.getDocumentFile(context, file)?.exists() ?: false
    }

    override fun mkDirs(file: File): Boolean {
        if (super.mkDirs(file))
            return true
        return DocumentUtils.getDocumentDirectory(context, file, true) != null
    }

    override fun delete(file: File): Boolean {
        if (super.delete(file))
            return true
        return DocumentUtils.getDocumentFile(context, file)?.delete() ?: false
    }

    override fun scan(file: File) {
        val paths = LinkedList<String>()
        if (isDirectory(file)) {
            walk(file, object : FileFilter {
                override fun accept(file: File) = true

                override fun file(file: File) {
                    if (!file.isDirectory && !file.name.contains(".parts")) {
                        paths.add(file.path)
                    }
                }

            })
        } else {
            paths.add(file.path)
        }

        if (paths.size > 0) {
            Librarian.getInstance().safePost(Runnable {
                MediaScanner.scanFiles(context, paths)
                // TODO: 2020/12/30 更新界面文件列表
            })
        }
    }

    override fun copy(src: File, dest: File): Boolean {
        if (super.copy(src, dest))
            return true

        val srcDocumentFile = DocumentUtils.getFile(context, src, false)
        val destDocumentFile = DocumentUtils.getFile(context, dest, false)

        if (srcDocumentFile == null) {
            DDLog.e("Unable to obtain document for file: $src")
            return false
        }
        if (destDocumentFile == null) {
            DDLog.e("Unable to obtain document for file: $dest")
            return false
        }

        return DocumentUtils.copyDocumentFile(context, srcDocumentFile, destDocumentFile)
    }

    override fun write(file: File, data: ByteArray): Boolean {
        if (super.write(file, data))
            return true

        val documentFile = DocumentUtils.getFile(context, file, true)
        if (documentFile == null) {
            DDLog.e("Unable to obtain document for file: $file")
            return false
        }

        return DocumentUtils.writeData(context, documentFile, data)
    }

    override fun write(file: File, inputStream: InputStream, notClose: Boolean): Boolean {
        if (super.write(file, inputStream, notClose))
            return true

        val documentFile = DocumentUtils.getFile(context, file, true)
        if (documentFile == null) {
            DDLog.e("Unable to obtain document for file: $file")
            return false
        }

        return DocumentUtils.writeData(context, documentFile, inputStream, notClose)
    }

    override fun read(file: File): ByteArray? {
        val byteArray = super.read(file)
        if (byteArray != null)
            return byteArray

        val documentFile = DocumentUtils.getFile(context, file, false)
        if (documentFile == null) {
            DDLog.e("Unable to obtain document for file: $file")
            return null
        }

        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(documentFile.uri) ?: return null
            val dataArray = ByteArray(inputStream.available())
            inputStream.read(dataArray)
            return dataArray
        } catch (t: Throwable){
            DDLog.e("Unable open document inputStream: $file", t)
        } finally {
            IOUtils.closeIO(inputStream)
        }
        return null
    }

    override fun listFiles(file: File, filter: FileFilter?): Array<File>? {
        val files = super.listFiles(file, filter)
        if (files != null) {
            return files
        }

        val documentDirectory = DocumentUtils.getDocumentDirectory(context, file) ?: return null
        val childFiles = documentDirectory.listFiles()
        val result = mutableListOf<File>()
        for (childFile in childFiles) {
            val documentPath = DocumentUtils.getDocumentPath(context, childFile.uri) ?: continue
            val storageFile = File(documentPath)
            if (filter?.accept(file) == true) {
                result.add(storageFile)
            }
        }
        return result.toTypedArray()
    }

    override fun openFd(file: File, mode: String): Int {
        val superFd = super.openFd(file, mode)
        if (superFd >= 0)
            return superFd

        if ("r" != mode && "w" != mode && "rw" != mode) {
            DDLog.e("Only r, w or rw modes supported")
            return -1
        }

        val documentFile = DocumentUtils.getFile(context, file, true)
        if (documentFile == null) {
            DDLog.e("Unable to obtain document for file: $file")
            return -1
        }
        return try {
            val fd =
                context.contentResolver.openFileDescriptor(documentFile.uri, mode) ?: return -1
            fd.detachFd()
        } catch (e: Throwable) {
            DDLog.e("Unable to get native fd", e)
            -1
        }
    }
}