package com.xyoye.common_component.base

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.view.animation.LinearInterpolator
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.DialogBaseLoadingBinding

/**
 * Created by xyoye on 2020/4/15.
 */

class BaseLoadingDialog(context: Context, msg: String = "加载中") :
    Dialog(context, R.style.LoadingDialog) {

    val dialogText: ObservableField<String> = ObservableField(msg)

    private var cycleAnimator: ObjectAnimator

    init {
        val dataBinding: DialogBaseLoadingBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_base_loading, null, false)
        setContentView(dataBinding.root)

        window?.setWindowAnimations(R.style.LoadingDialog_Animation)

        dataBinding.msgTv.isGone = msg.isEmpty()

        cycleAnimator = ObjectAnimator.ofFloat(dataBinding.progressIv, "rotation", 360f)
        cycleAnimator.run {
            duration = 1500
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    override fun dismiss() {
        cycleAnimator.cancel()
        super.dismiss()
    }
}