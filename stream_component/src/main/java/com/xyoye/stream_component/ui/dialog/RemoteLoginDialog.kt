package com.xyoye.stream_component.ui.dialog

import androidx.appcompat.app.AppCompatActivity
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

class RemoteLoginDialog(
    private val activity: AppCompatActivity,
    private val originalStorage: MediaLibraryEntity?,
    private val addMediaStorage: (MediaLibraryEntity) -> Unit,
    private val testConnect: (MediaLibraryEntity) -> Unit,
    private val testConnectResult: MutableLiveData<Boolean>,
    private val scanQRCode: () -> Unit
) : BaseBottomDialog<DialogRemoteLoginBinding>(activity) {

    private var remoteData: MediaLibraryEntity
    private var tokenRequired = false

    private lateinit var binding: DialogRemoteLoginBinding

    init {
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

        setGroupMode(remoteData.remoteAnimeGrouping)

        binding.scanLl.setOnClickListener {
            scanQRCode.invoke()
        }

        binding.serverTestConnectTv.setOnClickListener {
            if (checkParams(remoteData)) {
                testConnect.invoke(remoteData)
            }
        }

        binding.tvGroupByFile.setOnClickListener {
            remoteData.remoteAnimeGrouping = false
            setGroupMode(false)
        }

        binding.tvGroupByAnime.setOnClickListener {
            remoteData.remoteAnimeGrouping = true
            setGroupMode(true)
        }

        testConnectResult.observe(activity) {
            if (it) {
                binding.serverStatusTv.text = "连接成功"
                binding.serverStatusTv.setTextColorRes(R.color.text_blue)
            } else {
                binding.serverStatusTv.text = "连接失败"
                binding.serverStatusTv.setTextColorRes(R.color.text_red)
            }
        }

        setPositiveListener {
            if (checkParams(remoteData)) {
                addMediaStorage.invoke(remoteData)
                dismiss()
            }
        }

        setNegativeListener {
            dismiss()
        }

        setOnDismissListener {
            activity.finish()
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

    private fun setGroupMode(isGroupByAnime: Boolean) {
        binding.tvGroupByAnime.isSelected = isGroupByAnime
        binding.tvGroupByAnime.setTextColorRes(
            if (isGroupByAnime) R.color.text_white else R.color.text_black
        )

        binding.tvGroupByFile.isSelected = !isGroupByAnime
        binding.tvGroupByFile.setTextColorRes(
            if (!isGroupByAnime) R.color.text_white else R.color.text_black
        )
    }
}