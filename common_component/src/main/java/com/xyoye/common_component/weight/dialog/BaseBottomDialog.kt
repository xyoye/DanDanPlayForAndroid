package com.xyoye.common_component.weight.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.DialogBaseBottomDialogBinding
import com.xyoye.common_component.extension.rippleDrawable
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.utils.DialogFragmentHelper
import com.xyoye.common_component.utils.dp2px


/**
 * Created by xyoye on 2020/12/22.
 */

abstract class BaseBottomDialog<T : ViewDataBinding> : BottomSheetDialogFragment {
    private lateinit var mDialog: Dialog

    protected var onDialogDismiss: (() -> Unit)? = null
    protected var mOwnerActivity: AppCompatActivity? = null
    protected lateinit var rootViewBinding: DialogBaseBottomDialogBinding

    constructor() : super()

    constructor(anyValue: Boolean) : super() {
        arguments = DialogFragmentHelper.buildArgument()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mDialog = AppCompatDialog(context, theme)
        if (DialogFragmentHelper.isArgumentInvalid(arguments)) {
            //如果弹窗被重建，关闭弹窗
            dismiss()
            return mDialog
        }

        val layoutInflater = LayoutInflater.from(context)
        rootViewBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.dialog_base_bottom_dialog,
            null,
            false
        )

        val childViewBinding = DataBindingUtil.inflate<T>(
            layoutInflater,
            getChildLayoutId(),
            rootViewBinding.containerFl,
            true
        )

        mDialog.apply {
            window?.apply {
                decorView.setPadding(0, decorView.top, 0, decorView.bottom)

                val layoutParams = attributes
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                attributes = layoutParams

                setGravity(Gravity.BOTTOM)
                setWindowAnimations(R.style.BaseBottomDialogStyle)
                setBackgroundDrawable(R.drawable.background_bottom_dialog.toResDrawable())
            }

            setContentView(rootViewBinding.root)
        }

        //默认不允许从外部关闭
        mDialog.setCanceledOnTouchOutside(false)
        isCancelable = false

        initView(childViewBinding)

        return mDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismiss?.invoke()
    }

    open fun show(activity: AppCompatActivity) {
        mOwnerActivity = activity
        super.show(activity.supportFragmentManager, this::class.simpleName)
    }

    open fun show(fragment: Fragment) {
        mOwnerActivity = null
        super.show(fragment.childFragmentManager, this::class.simpleName)
    }

    protected fun setTitle(text: String) {
        rootViewBinding.titleTv.text = text
    }

    protected fun setPositiveText(text: String) {
        rootViewBinding.positiveBt.text = text
    }

    protected fun setNegativeText(text: String) {
        rootViewBinding.negativeBt.text = text
    }

    protected fun setPositiveListener(block: () -> Unit) {
        rootViewBinding.positiveBt.setOnClickListener { block.invoke() }
    }

    protected fun setNegativeListener(block: () -> Unit) {
        rootViewBinding.negativeBt.setOnClickListener { block.invoke() }
    }

    protected fun setPositiveVisible(visible: Boolean) {
        rootViewBinding.positiveBt.isVisible = visible
    }

    protected fun setNegativeVisible(visible: Boolean) {
        rootViewBinding.negativeBt.isVisible = visible
    }

    protected fun removeParentPadding() {
        rootViewBinding.containerFl.setPadding(0)
    }

    protected fun addNeutralButton(text: String, block: () -> Unit) {
        rootViewBinding.neutralBt.apply {
            isVisible = true
            setText(text)
            setOnClickListener { block.invoke() }
        }
    }

    protected fun addLeftAction(
        drawable: Drawable?,
        paddingDp: Int = 6,
        description: String = ""
    ): View {
        val actionView = createActionView(drawable, description, paddingDp)
        rootViewBinding.actionLeftContainer.apply {
            when (childCount) {
                0 -> {
                    val layoutParams = actionLayoutParams(Gravity.START)
                    addView(actionView, layoutParams)
                }
                1 -> {
                    val layoutParams = actionLayoutParams(Gravity.END)
                    addView(actionView, layoutParams)
                }
            }
        }
        return actionView
    }

    protected fun addRightAction(
        drawable: Drawable?,
        paddingDp: Int = 6,
        description: String = ""
    ): View {
        val actionView = createActionView(drawable, description, paddingDp)
        rootViewBinding.actionRightContainer.apply {
            when (childCount) {
                0 -> {
                    val layoutParams = actionLayoutParams(Gravity.END)
                    addView(actionView, layoutParams)
                }
                1 -> {
                    val layoutParams = actionLayoutParams(Gravity.START)
                    addView(actionView, layoutParams)
                }
            }
        }
        return actionView
    }

    private fun createActionView(drawable: Drawable?, description: String, paddingDp: Int): View {
        val padding = dp2px(paddingDp)
        val actionView = ImageView(context)
        actionView.setImageDrawable(drawable)
        actionView.setPadding(padding, padding, padding, padding)
        actionView.background = rippleDrawable()
        actionView.contentDescription = description
        return actionView
    }

    private fun actionLayoutParams(gravity: Int): FrameLayout.LayoutParams {
        val size = dp2px(36)
        val layoutParams = FrameLayout.LayoutParams(size, size)
        layoutParams.gravity = gravity
        return layoutParams
    }

    protected fun setDialogCancelable(touchCancel: Boolean, backPressedCancel: Boolean) {
        mDialog.setCanceledOnTouchOutside(touchCancel)
        isCancelable = backPressedCancel
    }

    abstract fun getChildLayoutId(): Int

    abstract fun initView(binding: T)
}