package com.xyoye.download_component.frostwire.utils

import androidx.core.text.isDigitsOnly
import com.frostwire.jlibtorrent.SessionParams
import com.frostwire.jlibtorrent.SettingsPack
import com.frostwire.jlibtorrent.Vectors
import com.frostwire.jlibtorrent.swig.*
import com.xyoye.common_component.storage.platform.AndroidPlatform
import com.xyoye.common_component.utils.AppUtils
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.download_component.frostwire.TorrentEngine
import java.io.File

/**
 * Created by xyoye on 2020/12/29.
 */

object EngineUtils {
    private val TAG = TorrentEngine::class.java.simpleName

    private const val NATIVE_VERSION_KEY = "state_version"
    private const val NATIVE_VERSION_VALUE = "1.2.0.6"

    /**
     * 加载配置
     */
    fun loadSettings(): SessionParams {
        val settingsFile = PathHelper.getDownloadSettingsFile()
        try {
            if (settingsFile.exists()) {
                val data = settingsFile.readBytes()
                val encodeData = Vectors.bytes2byte_vector(data)
                val decodeData = bdecode_node()
                val error = error_code()
                val decodeCode = bdecode_node.bdecode(encodeData, decodeData, error)
                if (decodeCode == 0) {
                    //为啥读出来的版本还是1.2.0.6？
                    val nativeVersion = decodeData.dict_find_string_value_s(NATIVE_VERSION_KEY)
                    if (NATIVE_VERSION_VALUE == nativeVersion) {
                        val paramsData = libtorrent.read_session_params(decodeData)
                        encodeData.clear()
                        return SessionParams(paramsData)
                    }
                } else {
                    DDLog.e(TAG, "decode session data failed: ${error.message()}")
                }
            }
        } catch (t: Throwable) {
            DDLog.e(TAG, "load session data failed", t)
        }
        return SessionParams(getDefaultSettings())
    }

    /**
     * 默认配置
     */
    fun getDefaultSettings(): SettingsPack {
        return SettingsPack().apply {
            //在libTorrent 1.2.4种被移除. 2020.2.10
            //broadcastLSD(true)
            maxQueuedDiskBytes(maxQueuedDiskBytes() / 2)
            sendBufferWatermark(sendBufferWatermark() / 2)
            cacheSize(26)
            //活动任务数量
            activeDownloads(3)
            //活动做种数量
            activeSeeds(3)
            //最大块连接数量
            maxPeerlistSize(200)
            //最大总连接数量
            connectionsLimit(200)
            //不活跃超时
            inactivityTimeout(60)
            //是否做种
            seedingOutgoingConnections(false)
            //数据更新间隔？
            tickInterval(1000)
        }
    }

    /**
     * 初始dht网络节点
     */
    fun getDHTBootstrapNodes(): String {
        val builder = StringBuilder()
        builder.append("dht.libtorrent.org:25401").append(",")
        builder.append("router.bittorrent.com:6881").append(",")
        builder.append("dht.transmissionbt.com:6881").append(",")
        // for DHT IPv6
        builder.append("router.silotis.us:6881")
        return builder.toString()
    }

    /**
     * 根据APP版本获取识别码数据
     */
    fun getFingerPrintData(): IntArray {
        // TODO: 2020/12/29 貌似每个识别码的区间是0-19，需要测试一下，避免版本号过大时有问题
        val versionName = AppUtils.getVersionName()
        val versionCode = AppUtils.getVersionCode()

        val invalidData = intArrayOf(0, 0, 0, 0)

        val versionData = versionName.split(".".toRegex())
        if (versionData.size != 3) {
            return invalidData
        }

        val data = IntArray(4)
        for ((index, value) in versionData.withIndex()) {
            if (value.isDigitsOnly()) {
                data[index] = value.toInt()
            } else {
                return invalidData
            }
        }
        data[3] = (versionCode % 10).toInt()
        return data
    }

    /**
     *  构建当前配置数据
     */
    fun buildSettings(session: session): ByteArray {
        val entry = entry()
        session.save_state(entry)
        entry.set(
            NATIVE_VERSION_KEY,
            NATIVE_VERSION_VALUE
        )
        return Vectors.byte_vector2bytes(entry.bencode())
    }

    /**
     * 保存配置
     */
    fun saveSettings(byteArray: ByteArray) {
        IOUtils.writeByteData(PathHelper.getDownloadSettingsFile().absolutePath, byteArray, false)
    }

    /**
     * 检查保存路径
     */
    fun checkSaveDirectory(directory: File): File? {
        val fileSystem = AndroidPlatform.getInstance().getFileSystem()

        if (!fileSystem.isDirectory(directory) && !fileSystem.mkDirs(directory)) {
            DDLog.w("Failed to create save dir to download")
            return null
        }

        if (!fileSystem.canWrite(directory)) {
            DDLog.w("Failed to setup save dir with write access")
            return null
        }

        return directory
    }
}