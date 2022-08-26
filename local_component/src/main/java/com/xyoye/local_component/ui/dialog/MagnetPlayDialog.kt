package com.xyoye.local_component.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogMagnetPlayBinding

class MagnetPlayDialog(
    activity: AppCompatActivity,
    private val magnetCallback: (magnetLink: String) -> Unit,
    private val torrentCallback: () -> Unit
) : BaseBottomDialog<DialogMagnetPlayBinding>(activity) {

    private lateinit var binding: DialogMagnetPlayBinding

    override fun getChildLayoutId() = R.layout.dialog_magnet_play

    override fun initView(binding: DialogMagnetPlayBinding) {

        this.binding = binding

        setTitle("新增磁链/种子播放")

        setNegativeListener { dismiss() }

        setPositiveListener {
            val result = binding.magnetInputEt.text.toString()
            if (result.isEmpty()) {
                ToastCenter.showWarning("磁链不能为空")
                return@setPositiveListener
            }

            magnetCallback.invoke(result)
            dismiss()
        }

        binding.torrentSelectBt.setOnClickListener {
            torrentCallback.invoke()
            dismiss()
        }
    }

    override fun dismiss() {
        if (this::binding.isInitialized){
            hideKeyboard(binding.magnetInputEt)
        }
        super.dismiss()
    }

}