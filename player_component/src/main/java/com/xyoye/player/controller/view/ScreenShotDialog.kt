package com.xyoye.player.controller.view

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.player.utils.getShotImageName
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.DialogScreenShotBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by xyoye on 2020/11/16.
 */

class ScreenShotDialog(private val mContext: Context, private val bitmap: Bitmap) :
    Dialog(mContext, R.style.StyleScreenShotDialog) {

    private lateinit var dialogBinding: DialogScreenShotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mContext),
            R.layout.dialog_screen_shot,
            null,
            false
        )

        setContentView(dialogBinding.root)

        dialogBinding.shotIv.setImageBitmap(bitmap)

        dialogBinding.shotCancelBt.setOnClickListener {
            dismiss()
        }

        dialogBinding.shotSaveBt.setOnClickListener {
            if (saveShotImage(bitmap)) {
                ToastCenter.showOriginalToast("保存截图成功")
            } else {
                ToastCenter.showOriginalToast("保存截图失败")
            }
            dismiss()
        }
    }

    override fun show() {
        super.show()

        val layoutParams = window?.attributes ?: return
        layoutParams.apply {
            gravity = Gravity.CENTER
            width = dp2px(450)
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window?.apply {
            decorView.setPadding(0, 0, 0, 0)
            attributes = layoutParams
        }
    }

    override fun dismiss() {
        super.dismiss()

        if (mContext is AppCompatActivity) {
            ImmersionBar.with(mContext)
                .fullScreen(true)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init()
        }
    }

    private fun saveShotImage(bitmap: Bitmap): Boolean {
        var isSuccess = false
        var fileOutputStream: FileOutputStream? = null
        try {
            val saveFile = File(PathHelper.getScreenShotDirectory(), getShotImageName())
            if (saveFile.exists())
                saveFile.delete()
            saveFile.createNewFile()

            fileOutputStream = FileOutputStream(saveFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            isSuccess = true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeIO(fileOutputStream)
        }
        return isSuccess
    }
}