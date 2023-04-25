package com.xyoye.stream_component.utils.screencast.provider

import android.text.TextUtils
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.EntropyUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.bean.UDPDeviceBean
import com.xyoye.stream_component.utils.screencast.receiver.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  :
 * </pre>
 */

object UdpClient {
    private var TAG = UdpClient::class.java.simpleName

    private var multicastSocket: MulticastSocket? = null

    private var receiveCallback: (suspend (UDPDeviceBean) -> Unit)? = null

    private var isRunning = AtomicBoolean(false)

    /**
     * 启动组播接收
     */
    suspend fun startMulticastReceive(callback: suspend (UDPDeviceBean) -> Unit) {
        stopMulticastReceive()

        if (initMulticastSocket().not()) {
            return
        }

        this.receiveCallback = callback
        isRunning.set(true)
        while (isRunning.get()) {
            receiveMulticast()
        }
    }

    /**
     * 停止组播接收
     */
    fun stopMulticastReceive() {
        isRunning.set(false)
        receiveCallback = null
        IOUtils.closeIO(multicastSocket)
        multicastSocket = null
    }

    /**
     * 初始化组播Socket
     */
    private fun initMulticastSocket(): Boolean {
        try {
            val multicastHostName = UdpServer.multicastHostName
            val group = InetAddress.getByName(UdpServer.multicastHostName)
            if (!group.isMulticastAddress) {
                DDLog.e(TAG, "${multicastHostName}不是组播地址")
                return false
            }
            multicastSocket = MulticastSocket(UdpServer.multicastPort)
            multicastSocket!!.joinGroup(group)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (multicastSocket == null) {
            return false
        }

        return true
    }

    /**
     * 接收UDP组播
     */
    private suspend fun receiveMulticast() {
        val buffer = ByteArray(1024)
        val datagramPacket = DatagramPacket(buffer, buffer.size)

        try {
            withContext(Dispatchers.IO) {
                multicastSocket?.receive(datagramPacket)
            }

            //组播消息
            val entropyMsg = String(datagramPacket.data, 0, datagramPacket.length)
            if (entropyMsg.isEmpty()) {
                return
            }
            //组播消息解密
            val msg = EntropyUtils.aesDecode(UdpServer.multicastMsgKey, entropyMsg)
            if (TextUtils.isEmpty(msg)) {
                return
            }
            val udpDeviceBean = JsonHelper.parseJson<UDPDeviceBean>(msg!!) ?: return
            val socketAddress = datagramPacket.socketAddress as InetSocketAddress
            udpDeviceBean.ipAddress = socketAddress.hostName

            receiveCallback?.invoke(udpDeviceBean)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}