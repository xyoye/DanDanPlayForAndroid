package com.xyoye.storage_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.xyoye.common_component.adapter.BaseViewHolderCreator
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.application.DanDanPlay
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.UDPDeviceBean
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.DialogScreencastConnectBinding
import com.xyoye.storage_component.databinding.ItemScreencastReceiveDeviceBinding
import com.xyoye.storage_component.ui.activities.storage_plus.StoragePlusActivity
import com.xyoye.storage_component.utils.launcher.ScanActivityLauncher
import com.xyoye.storage_component.utils.screencast.provider.UdpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by xyoye on 2022/7/23.
 */

class ScreencastStorageEditDialog(
    private val activity: StoragePlusActivity,
    private val originalStorage: MediaLibraryEntity?
) : StorageEditDialog<DialogScreencastConnectBinding>(activity) {

    private lateinit var binding: DialogScreencastConnectBinding
    private var isEditStorage: Boolean = false

    private val scanDevices = mutableListOf<UDPDeviceBean>()
    private var scanDeviceJob: Job? = null

    private val scanActivityLauncher = ScanActivityLauncher(activity, onScanResult())
    private var testLibrary: MediaLibraryEntity? = null

    override fun getChildLayoutId() = R.layout.dialog_screencast_connect

    override fun initView(binding: DialogScreencastConnectBinding) {
        this.binding = binding
        isEditStorage = originalStorage != null

        //禁止拖动关闭弹窗
        disableSheetDrag()
        setTitle(if (isEditStorage) "编辑投屏信息" else "新增投屏设备")

        val serverData = originalStorage ?: MediaLibraryEntity(
            0,
            "",
            "",
            MediaType.SCREEN_CAST
        )
        binding.serverData = serverData
        binding.ipEt.setText(serverData.url.split(":").getOrNull(0))

        initListener()

        initView()

        initDeviceRv()

        setOnShowListener {
            startDeviceScan()
        }
    }

    private fun initListener() {
        setPositiveListener {
            val displayName = binding.displayNameEt.text.toString()
            val host = binding.ipEt.text.toString()
            val port = binding.portEt.text.toString()
            val password = binding.passwordEt.text.toString()
            connect(host, port, displayName, password)
        }

        setNegativeListener {
            activity.finish()
        }

        binding.tvAutoConnect.setOnClickListener {
            switchStyle(editMode = false)
        }

        binding.tvInputConnect.setOnClickListener {
            switchStyle(editMode = true)
        }

        binding.tvScanConnect.setOnClickListener {
            DanDanPlay.permission.camera.request(activity) {
                onGranted {
                    scanActivityLauncher.launch()
                }
                onDenied {
                    ToastCenter.showError("获取相机权限失败，无法进行扫码")
                }
            }
        }

        binding.passwordConnectTv.setOnClickListener {
            switchStyle(needPassword = true)
        }

        binding.directConnectTv.setOnClickListener {
            switchStyle(needPassword = false)
        }

        binding.passwordToggleIv.setOnClickListener {
            if (binding.passwordToggleIv.isSelected) {
                binding.passwordToggleIv.isSelected = false
                binding.passwordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            } else {
                binding.passwordToggleIv.isSelected = true
                binding.passwordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
        }
    }

    override fun onTestResult(result: Boolean) {
        if (testLibrary == null) {
            return
        }
        if (result) {
            activity.addStorage(testLibrary!!)
        }
    }

    override fun doBeforeDismiss() {
        stopDeviceScan()
        super.doBeforeDismiss()
    }

    private fun initView() {
        if (isEditStorage) {
            binding.tvAutoConnect.isEnabled = false
            binding.tvScanConnect.isEnabled = false
            switchStyle(
                editMode = true,
                needPassword = originalStorage?.password.isNullOrEmpty().not()
            )
        } else {
            switchStyle(editMode = false, needPassword = false)
        }
    }

    private fun initDeviceRv() {
        binding.deviceRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem(R.layout.item_screencast_receive_device) {
                    initView(deviceItem())
                }
            }
        }
    }

    private fun BaseViewHolderCreator<ItemScreencastReceiveDeviceBinding>.deviceItem() = { data: UDPDeviceBean ->
        itemBinding.device = data
        itemBinding.itemLayout.setOnClickListener {
            if (data.needPassword) {
                inputPassword(data)
                ToastCenter.showToast("请输入连接密码")
            } else {
                connect(data)
            }
        }
    }

    /**
     * 切换弹窗样式
     */
    private fun switchStyle(
        editMode: Boolean = binding.tvInputConnect.isSelected,
        needPassword: Boolean = binding.passwordConnectTv.isSelected
    ) {
        binding.tvAutoConnect.isSelected = editMode.not()
        binding.scanFl.isVisible = editMode.not()

        binding.tvInputConnect.isSelected = editMode
        binding.inputLl.isVisible = editMode
        rootViewBinding.positiveBt.isVisible = editMode

        binding.passwordEt.isEnabled = needPassword
        binding.passwordToggleIv.isEnabled = needPassword

        binding.passwordConnectTv.isSelected = needPassword
        binding.directConnectTv.isSelected = !needPassword

        if (needPassword.not()) {
            binding.passwordEt.setText("")
        }
    }

    /**
     * 连接至搜索到的设备时，需要密码
     */
    private fun inputPassword(device: UDPDeviceBean) {
        switchStyle(editMode = true, needPassword = device.needPassword)

        binding.ipEt.setText(device.ipAddress)
        binding.portEt.setText(device.httpPort.toString())
        binding.displayNameEt.setText(device.deviceName)
    }

    /**
     * 连接至投屏设备
     */
    private fun connect(ip: String, port: String, displayName: String, password: String) {
        if (ip.isEmpty()) {
            ToastCenter.showError("IP地址不能为空")
            return
        }
        if (port.isEmpty()) {
            ToastCenter.showError("端口不能为空")
            return
        }
        val httpPort = port.toIntOrNull()
        if (httpPort == null) {
            ToastCenter.showError("端口错误")
            return
        }
        if (password.isEmpty() && binding.passwordConnectTv.isSelected) {
            ToastCenter.showError("密码不能为空")
            return
        }
        var deviceName = displayName
        if (displayName.isEmpty()) {
            deviceName = "未知投屏设备"
        }
        val device = UDPDeviceBean(ip, httpPort, deviceName)
        connect(device, password)
    }

    /**
     * 连接至投屏设备
     */
    private fun connect(device: UDPDeviceBean, password: String? = null) {
        val library = MediaLibraryEntity(
            displayName = device.deviceName,
            url = "http://${device.ipAddress}:${device.httpPort}",
            screencastAddress = device.ipAddress ?: "",
            port = device.httpPort,
            password = password,
            mediaType = MediaType.SCREEN_CAST
        )
        testLibrary = library
        activity.testStorage(library)
    }

    private fun startDeviceScan() {
        scanDeviceJob?.cancel()
        scanDeviceJob = activity.lifecycleScope.launch(Dispatchers.IO) {
            UdpClient.startMulticastReceive {
                withContext(Dispatchers.Main) {
                    onDeviceFound(it)
                }
            }
        }
    }

    private fun stopDeviceScan() {
        UdpClient.stopMulticastReceive()
        scanDeviceJob?.cancel()
    }

    private fun onDeviceFound(device: UDPDeviceBean) {
        if (isShowing.not() || activity.isFinishing || activity.isDestroyed) {
            return
        }
        if (device.ipAddress.isNullOrEmpty() || device.httpPort == 0) {
            return
        }
        val addedDevice = scanDevices.find {
            it.ipAddress == device.ipAddress && it.httpPort == device.httpPort
        }
        if (addedDevice != null) {
            scanDevices.remove(addedDevice)
        }
        scanDevices.add(device)
        scanDevices.sortBy { it.ipAddress + it.httpPort }
        binding.deviceRv.setData(scanDevices)
    }

    private fun onScanResult() = block@{ data: RemoteScanData? ->
        if (data == null) {
            return@block
        }
        switchStyle(editMode = true, needPassword = data.tokenRequired)

        binding.ipEt.setText(data.selectedIP)
        binding.portEt.setText(data.port.toString())
        binding.displayNameEt.setText(data.machineName)

        if (data.tokenRequired) {
            ToastCenter.showToast("请输入连接密码")
        }
    }
}