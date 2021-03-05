package com.xyoye.common_component.weight.dialog

import android.content.DialogInterface
import android.os.CountDownTimer
import androidx.core.view.isVisible
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.DialogCommonBinding
import com.xyoye.common_component.extension.setTextColorRes
import java.util.*

/**
 * Created by xyoye on 2020/10/28.
 */

open class CommonDialog : BaseBottomDialog<DialogCommonBinding> {

    private lateinit var mBuilder: Builder

    constructor(): super()

    private constructor(builder: Builder): super(true){
        mBuilder = builder
    }

    private val delayTimer = object : CountDownTimer(5000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
            rootViewBinding.positiveBt.isEnabled = false
            val time = (millisUntilFinished / 1000L).toInt() + 1
            val timeText = "$time S"
            rootViewBinding.positiveBt.setTextColorRes(R.color.text_gray)
            setPositiveText(timeText)
        }

        override fun onFinish() {
            rootViewBinding.positiveBt.isEnabled = true
            rootViewBinding.positiveBt.setTextColorRes(R.color.text_theme)
            setPositiveText("确定")
        }
    }

    open class Builder {
        var tips: String? = null
        var content: String = ""
        var cancelable = true
        var touchCancelable = true
        var delayConfirm = false
        var negativeText: String? = null
        var negativeClickListener: ((CommonDialog) -> Unit)? = null
        var positiveText: String? = null
        var positiveClickListener: ((CommonDialog) -> Unit)? = null
        var noShowAgain: Boolean = false
        var noShowAgainListener: ((Boolean) -> Unit)? = null

        open fun addNegative(
            negativeText: String = "取消",
            negativeClickListener: ((CommonDialog) -> Unit) = { dialog -> dialog.dismiss() }
        ): Builder {
            this.negativeClickListener = negativeClickListener
            this.negativeText = negativeText
            return this
        }

        open fun addPositive(
            positiveText: String = "确定",
            positiveClickListener: ((CommonDialog) -> Unit)
        ): Builder {
            this.positiveClickListener = positiveClickListener
            this.positiveText = positiveText
            return this
        }

        open fun addNoShowAgain(listener: (Boolean) -> Unit) {
            noShowAgain = true
            noShowAgainListener = listener
        }

        fun build(): CommonDialog =
            CommonDialog(this)
    }

    override fun getChildLayoutId() = R.layout.dialog_common

    override fun initView(binding: DialogCommonBinding) {

        mBuilder.apply {

            setTitle(tips ?: "提示")

            binding.contentTv.text = content

            setDialogCancelable(touchCancelable, cancelable)

            setPositiveVisible(false)
            setNegativeVisible(false)

            negativeText?.let {
                setNegativeVisible(true)
                setNegativeText(it)
            }
            positiveText?.let {
                setPositiveVisible(true)
                setPositiveText(it)
            }

            binding.noShowAgainCb.isVisible = noShowAgain
            binding.noShowAgainCb.setOnCheckedChangeListener { _, isChecked ->
                noShowAgainListener?.invoke(isChecked)
            }

            setNegativeListener { negativeClickListener?.invoke(this@CommonDialog) }

            setPositiveListener { positiveClickListener?.invoke(this@CommonDialog) }

            if (delayConfirm) {
                delayTimer.start()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        delayTimer.cancel()
        super.onDismiss(dialog)
    }
}