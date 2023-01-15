package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.WebDavStorageFile
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.sardine.DavResource
import com.xyoye.sardine.impl.OkHttpSardine
import com.xyoye.sardine.util.SardineConfig
import okhttp3.Credentials
import java.io.InputStream
import java.net.URI
import java.util.*

/**
 * Created by xyoye on 2022/12/29
 */

class WebDavStorage(
    library: MediaLibraryEntity
) : AbstractStorage(library) {

    private val sardine = OkHttpSardine(UnsafeOkHttpClient.client)

    init {
        SardineConfig.isXmlStrictMode = this.library.webDavStrict
        getAccountInfo()?.let {
            sardine.setCredentials(it.first, it.second)
        }
    }

    override suspend fun getRootFile(): StorageFile {
        val rootPath = Uri.parse(library.url).path ?: "/"
        return pathFile(rootPath)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        return try {
            sardine.get(file.fileUrl())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        return try {
            sardine.list(file.fileUrl())
                .filter { isChildFile(file.fileUrl(), it.href) }
                .map { WebDavStorageFile(it, this) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun pathFile(path: String): StorageFile {
        val hrefUrl = resolvePath(path).toString()
        val davResource = CustomDavResource(hrefUrl)
        return WebDavStorageFile(davResource, this)
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.fileUrl()
    }

    override fun getNetworkHeaders(): Map<String, String>? {
        val accountInfo = getAccountInfo()
            ?: return null
        val credential = Credentials.basic(accountInfo.first, accountInfo.second)
        return mapOf(Pair("Authorization", credential))
    }

    private fun getAccountInfo(): Pair<String, String>? {
        if (library.account.isNullOrEmpty()) {
            return null
        }
        return Pair(library.account ?: "", library.password ?: "")
    }

    private fun isChildFile(parent: String, child: URI): Boolean {
        try {
            val parentPath = URI(parent).path
            val childPath = child.path
            return parentPath != childPath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private class CustomDavResource(href: String, isDirectory: Boolean = true) : DavResource(
        href,
        Date(),
        Date(),
        if (isDirectory) "httpd/unix-directory" else "application/octet-stream",
        0,
        "",
        "",
        emptyList(),
        "",
        emptyList(),
        emptyMap()
    )
}