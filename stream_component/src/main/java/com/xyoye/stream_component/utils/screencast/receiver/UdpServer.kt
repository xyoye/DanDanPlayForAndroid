package com.xyoye.stream_component.utils.screencast.receiver

import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.EntropyUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.bean.UDPDeviceBean
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  :
 * </pre>
 */

object UdpServer {

    private var TAG = UdpServer::class.java.simpleName

    const val multicastPort = 12333
    const val multicastHostName = "239.254.254.254"
    const val multicastMsgKey = "03YSdjQY7q3bDdnq"
    private const val multicastIntervalMs = 2000L

    //UDPSocket
    private var multicastSocket: MulticastSocket? = null

    //组播地址
    private var multicastAddress: InetAddress? = null

    //组播次数
    private var multicastCount = 0

    private var isRunning = AtomicBoolean(false)

    /**
     * 启动组播发送
     */
    suspend fun startMulticastEmit(httpPort: Int, needPassword: Boolean) {
        stopMulticastEmit()

        if (initMulticastSocket().not()) {
            return
        }

        isRunning.set(true)
        multicastCount = 0
        while (isRunning.get()) {
            sendMulticast(httpPort, needPassword)
            delay(timeMillis = multicastIntervalMs)
        }
    }

    /**
     * 停止组播发送
     */
    fun stopMulticastEmit() {
        isRunning.set(false)
        IOUtils.closeIO(multicastSocket)
        multicastSocket = null
    }

    /**
     * 初始化组播Socket
     */
    private fun initMulticastSocket(): Boolean {
        try {
            multicastAddress = InetAddress.getByName(multicastHostName)
            if (!multicastAddress!!.isMulticastAddress) {
                DDLog.e(TAG, "${multicastHostName}不是组播地址")
                return false
            }
            multicastSocket = MulticastSocket()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (multicastSocket == null) {
            return false
        }

        return true
    }

    /**
     * 发送组播
     */
    private fun sendMulticast(httpPort: Int, needPassword: Boolean) {
        try {
            val udpDeviceBean = UDPDeviceBean(
                httpPort = httpPort,
                deviceName = getDeviceName(),
                count = multicastCount,
                needPassword = needPassword
            )
            val msg = JsonHelper.toJson(udpDeviceBean) ?: return

            //组播内容加密
            val entropyMsg = EntropyUtils.aesEncode(multicastMsgKey, msg)
            if (TextUtils.isEmpty(entropyMsg)) {
                return
            }
            DDLog.i(UdpServer::class.java.simpleName, entropyMsg!!)

            val data = entropyMsg.toByteArray()

            val sendPacket =
                DatagramPacket(data, data.size, multicastAddress, multicastPort)
            multicastSocket?.send(sendPacket)

            multicastCount++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取设备名称
     */
    private fun getDeviceName(): String {
        var deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Settings.Global.getString(
                BaseApplication.getAppContext().contentResolver,
                Settings.Global.DEVICE_NAME
            )
        } else {
            null
        }
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = "${Build.BRAND}_${Build.DEVICE}"
        }
        return deviceName!!
    }
}