package com.xyoye.common_component.weight

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
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

class BottomActionDialog(
    activity: Activity,
    private val actionData: MutableList<SheetActionBean>,
    private val title: String? = null,
    private val callback: (Int) -> Boolean
) : BaseBottomDialog<DialogBottomActionBinding>(activity) {

    var onNegativeCallback: (() -> Unit)? = null

    override fun getChildLayoutId() = R.layout.dialog_bottom_action

    override fun initView(binding: DialogBottomActionBinding) {
        setTitle(title ?: "请选择操作")

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
                                if (callback.invoke(data.actionId)) dismiss()
                            }
                        }
                    }
                }

                setData(actionData)
            }
        }
    }
}