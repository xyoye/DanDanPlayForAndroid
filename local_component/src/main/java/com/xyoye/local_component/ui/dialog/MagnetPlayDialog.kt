package com.xyoye.local_component.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogMagnetPlayBinding

class MagnetPlayDialog(
    private val activity: AppCompatActivity
) : BaseBottomDialog<DialogMagnetPlayBinding>(activity) {

    private lateinit var binding: DialogMagnetPlayBinding

    override fun getChildLayoutId() = R.layout.dialog_magnet_play

    override fun initView(binding: DialogMagnetPlayBinding) {

        this.binding = binding

        setTitle("新增磁链/种子播放")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val magnetLink = binding.magnetInputEt.text.toString()
            if (magnetLink.isEmpty()) {
                ToastCenter.showWarning("磁链不能为空")
                return@setPositiveListener
            }
            launchStorageFileActivity(magnetLink)
            dismiss()
        }

        binding.torrentSelectBt.setOnClickListener {
            selectTorrentFile()
            dismiss()
        }
    }

    private fun selectTorrentFile() {
        FileManagerDialog(activity, FileManagerAction.ACTION_SELECT_TORRENT) { torrentPath ->
            launchStorageFileActivity(torrentPath)
        }.show()
    }

    private fun launchStorageFileActivity(link: String) {
        val library = MediaLibraryEntity.TORRENT.copy(url = link)
        ARouter.getInstance()
            .build(RouteTable.Stream.StorageFile)
            .withParcelable("storageLibrary", library)
            .navigation()
    }

    override fun dismiss() {
        if (this::binding.isInitialized) {
            hideKeyboard(binding.magnetInputEt)
        }
        super.dismiss()
    }

}