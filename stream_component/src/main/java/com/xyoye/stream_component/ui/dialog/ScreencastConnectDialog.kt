package com.xyoye.stream_component.ui.dialog

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.UDPDeviceBean
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogScreencastConnectBinding
import com.xyoye.stream_component.databinding.ItemScreencastReceiveDeviceBinding

/**
 * Created by xyoye on 2022/7/23.
 */

class ScreencastConnectDialog(
    private val activity: AppCompatActivity,
    private val originalStorage: MediaLibraryEntity?,
    private val devicesLiveData: LiveData<List<UDPDeviceBean>>,
    private val onConnectDevice: (UDPDeviceBean, String?) -> Unit,
    private val connectResult: LiveData<Boolean>,
    private val scanQRCode: () -> Unit,
) : BaseBottomDialog<DialogScreencastConnectBinding>(activity) {
    private lateinit var binding: DialogScreencastConnectBinding
    private var isEditStorage: Boolean = false

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

        initListener()

        initObserver()

        initView()

        initDeviceRv()
    }

    private fun initObserver() {
        devicesLiveData.observe(activity) {
            binding.deviceRv.setData(it)
        }

        connectResult.observe(activity) {
            if (it) {
                dismiss()
            }
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
            dismiss()
        }

        setOnDismissListener {
            activity.finish()
        }

        binding.tvAutoConnect.setOnClickListener {
            switchStyle(editMode = false)
        }

        binding.tvInputConnect.setOnClickListener {
            switchStyle(editMode = true)
        }

        binding.tvScanConnect.setOnClickListener {
            scanQRCode.invoke()
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
                addItem<UDPDeviceBean, ItemScreencastReceiveDeviceBinding>(R.layout.item_screencast_receive_device) {
                    initView { data, _, _ ->
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
                }
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
        onConnectDevice.invoke(device, password)
    }

    /**
     * 设置扫码结果
     */
    fun setScanResult(data: RemoteScanData) {
        switchStyle(editMode = true, needPassword = data.tokenRequired)

        binding.ipEt.setText(data.selectedIP)
        binding.portEt.setText(data.port.toString())
        binding.displayNameEt.setText(data.machineName)
    }
}