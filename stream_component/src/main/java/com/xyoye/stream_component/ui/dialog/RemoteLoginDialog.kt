package com.xyoye.stream_component.ui.dialog

import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogRemoteLoginBinding

/**
 * Created by xyoye on 2021/3/25.
 */

class RemoteLoginDialog : BaseBottomDialog<DialogRemoteLoginBinding> {
    private var originalStorage: MediaLibraryEntity? = null
    private lateinit var addMediaStorage: (MediaLibraryEntity) -> Unit
    private lateinit var testConnect: (MediaLibraryEntity) -> Unit
    private lateinit var testConnectResult: MutableLiveData<Boolean>
    private lateinit var scanQRCode: () -> Unit

    private lateinit var remoteData: MediaLibraryEntity
    private var tokenRequired = false

    private lateinit var binding: DialogRemoteLoginBinding

    constructor() : super()

    constructor(
        originalStorage: MediaLibraryEntity?,
        addMediaStorage: (MediaLibraryEntity) -> Unit,
        testConnect: (MediaLibraryEntity) -> Unit,
        testConnectResult: MutableLiveData<Boolean>,
        scanQRCode: () -> Unit
    ) : super(true) {
        this.originalStorage = originalStorage
        this.addMediaStorage = addMediaStorage
        this.testConnect = testConnect
        this.testConnectResult = testConnectResult
        this.scanQRCode = scanQRCode

        remoteData = originalStorage ?: MediaLibraryEntity(
            0,
            "",
            "",
            MediaType.REMOTE_STORAGE,
            port = 80
        )
    }

    override fun getChildLayoutId() = R.layout.dialog_remote_login

    override fun initView(binding: DialogRemoteLoginBinding) {
        this.binding = binding
        val isEditStorage = originalStorage != null

        setTitle(if (isEditStorage) "编辑远程连接帐号" else "添加远程连接帐号")
        binding.remoteData = remoteData

        binding.scanLl.setOnClickListener {
            scanQRCode.invoke()
        }

        binding.serverTestConnectIv.setOnClickListener {
            if (checkParams(remoteData)) {
                testConnect.invoke(remoteData)
            }
        }

        testConnectResult.observe(mOwnerActivity!!, {
            if (it) {
                binding.serverStatusTv.text = "连接成功"
                binding.serverStatusTv.setTextColorRes(R.color.text_blue)
            } else {
                binding.serverStatusTv.text = "连接失败"
                binding.serverStatusTv.setTextColorRes(R.color.text_red)
            }
        })

        setPositiveListener {
            if (checkParams(remoteData)) {
                addMediaStorage.invoke(remoteData)
                dismiss()
                mOwnerActivity?.finish()
            }
        }

        setNegativeListener {
            dismiss()
            mOwnerActivity?.finish()
        }
    }

    fun setScanResult(remoteScanData: RemoteScanData) {
        remoteData.url = remoteScanData.selectedIP ?: ""
        remoteData.port = remoteScanData.port
        remoteData.displayName = remoteScanData.machineName ?: ""
        tokenRequired = remoteScanData.tokenRequired
        binding.remoteData = remoteData
    }

    private fun checkParams(remoteData: MediaLibraryEntity): Boolean {
        if (remoteData.url.isEmpty()) {
            ToastCenter.showWarning("请填写IP地址")
            return false
        }

        if (tokenRequired && remoteData.remoteSecret.isNullOrEmpty()) {
            ToastCenter.showWarning("请填写API密钥")
            return false
        }
        return true
    }
}