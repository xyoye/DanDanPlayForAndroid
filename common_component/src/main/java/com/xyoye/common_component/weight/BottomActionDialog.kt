package com.xyoye.common_component.weight

import androidx.core.view.isGone
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.databinding.DialogBottomActionBinding
import com.xyoye.common_component.databinding.ItemBottomActionVerticalBinding
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.SheetActionBean

/**
 * Created by xyoye on 2020/11/18.
 */

class BottomActionDialog : BaseBottomDialog<DialogBottomActionBinding> {
    private lateinit var mActionData: MutableList<SheetActionBean>
    private var mTitle: String? = null
    private lateinit var mCallback: ((Int) -> Boolean)

    constructor() : super()

    constructor(
        actionData: MutableList<SheetActionBean>,
        title: String? = null,
        callback: (Int) -> Boolean
    ) : super(true) {
        mCallback = callback
        mActionData = actionData
        mTitle = title
    }

    var onNegativeCallback: (() -> Unit)? = null

    override fun getChildLayoutId() = R.layout.dialog_bottom_action

    override fun initView(binding: DialogBottomActionBinding) {
        setTitle(mTitle ?: "请选择操作")

        setNegativeListener {
            onNegativeCallback?.invoke()
            dismiss()
        }
        setPositiveVisible(false)

        removeParentPadding()

        binding.contentRv.apply {

            layoutManager = vertical()

            adapter = buildAdapter {

                addItem<SheetActionBean, ItemBottomActionVerticalBinding>(R.layout.item_bottom_action_vertical) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            actionNameTv.text = data.actionName
                            if (data.actionIconRes != -1) {
                                actionIv.setImageResource(data.actionIconRes)
                            }
                            actionDescribeTv.isGone = data.describe.isNullOrEmpty()
                            actionDescribeTv.text = data.describe
                            itemLayout.setOnClickListener {
                                if (mCallback.invoke(data.actionId)) dismiss()
                            }
                        }
                    }
                }

                setData(mActionData)
            }
        }
    }
}