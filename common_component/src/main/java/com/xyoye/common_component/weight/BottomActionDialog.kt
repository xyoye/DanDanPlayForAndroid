package com.xyoye.common_component.weight

import androidx.core.view.isGone
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.databinding.DialogBottomActionBinding
import com.xyoye.common_component.databinding.ItemBottomActionGridBinding
import com.xyoye.common_component.databinding.ItemBottomActionHorizontalBinding
import com.xyoye.common_component.databinding.ItemBottomActionVerticalBinding
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.enums.SheetActionType

/**
 * Created by xyoye on 2020/11/18.
 */

class BottomActionDialog : BaseBottomDialog<DialogBottomActionBinding> {
    private lateinit var mActionData: MutableList<SheetActionBean>
    private lateinit var mActionType : SheetActionType
    private var mTitle: String? = null
    private lateinit var mCallback: ((Int) -> Boolean)

    constructor(): super()

    constructor(actionData: MutableList<SheetActionBean>,
                actionType: SheetActionType,
                title: String? = null,
                callback: (Int) -> Boolean): super(true){
        mCallback = callback
        mActionData = actionData
        mActionType = actionType
        mTitle = title
    }

    var onNegativeCallback : (() -> Unit)? = null

    override fun getChildLayoutId() = R.layout.dialog_bottom_action

    override fun initView(binding: DialogBottomActionBinding) {
        setTitle(mTitle ?: "请选择操作")

        setNegativeListener {
            onNegativeCallback?.invoke()
            dismiss()
        }
        setPositiveVisible(false)

        val actionLayoutManager: LinearLayoutManager
        val itemLayoutId: Int

        when (mActionType) {
            SheetActionType.GRID -> {
                actionLayoutManager = GridLayoutManager(context, 2)
                itemLayoutId = R.layout.item_bottom_action_grid
            }
            SheetActionType.HORIZONTAL -> {
                actionLayoutManager = GridLayoutManager(context, mActionData.size)
                itemLayoutId = R.layout.item_bottom_action_horizontal
            }
            SheetActionType.VERTICAL -> {
                actionLayoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                itemLayoutId = R.layout.item_bottom_action_vertical
            }
        }

        binding.contentRv.apply {

            layoutManager = actionLayoutManager

            adapter = buildAdapter<SheetActionBean> {
                initData(mActionData)

                addItem<SheetActionBean, ViewDataBinding>(itemLayoutId) {
                    initView { data, _, _ ->
                        when (itemBinding) {
                            is ItemBottomActionGridBinding -> {
                                (itemBinding as ItemBottomActionGridBinding).apply {
                                    actionNameTv.text = data.actionName
                                    if (data.actionIconRes != -1) {
                                        actionIv.setImageResource(data.actionIconRes)
                                    }
                                    itemLayout.setOnClickListener {
                                        if (mCallback.invoke(data.actionId)) dismiss()
                                    }
                                }
                            }
                            is ItemBottomActionHorizontalBinding -> {
                                (itemBinding as ItemBottomActionHorizontalBinding).apply {
                                    actionNameTv.text = data.actionName
                                    if (data.actionIconRes != -1) {
                                        actionIv.setImageResource(data.actionIconRes)
                                    }
                                    itemLayout.setOnClickListener {
                                        if (mCallback.invoke(data.actionId)) dismiss()
                                    }
                                }
                            }
                            is ItemBottomActionVerticalBinding -> {
                                (itemBinding as ItemBottomActionVerticalBinding).apply {
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
                    }
                }
            }

            if (mActionType == SheetActionType.GRID) {
                addItemDecoration(ItemDecorationSpace(dp2px(10)))
            }
        }
    }
}