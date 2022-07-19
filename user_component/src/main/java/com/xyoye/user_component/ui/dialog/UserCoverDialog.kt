package com.xyoye.user_component.ui.dialog

import android.app.Activity
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.DialogUserCoverBinding
import com.xyoye.user_component.databinding.ItemUserCoverBinding

/**
 * Created by xyoye on 2021/1/6.
 */

class UserCoverDialog(
    private val activity: Activity,
    private val callback: (Int) -> Unit
) : BaseBottomDialog<DialogUserCoverBinding>(activity) {

    override fun getChildLayoutId() = R.layout.dialog_user_cover

    override fun initView(binding: DialogUserCoverBinding) {

        disableSheetDrag()

        setTitle("选择头像")

        binding.userCoverRv.apply {
            layoutManager = grid(4)

            adapter = buildAdapter {
                addItem<Int, ItemUserCoverBinding>(R.layout.item_user_cover) {
                    initView { data, position, _ ->
                        val drawableArray = resources.obtainTypedArray(R.array.cover)
                        val coverResId = drawableArray.getResourceId(data, 0)
                        drawableArray.recycle()
                        itemBinding.userCoverIv.setImageResource(coverResId)
                        val nameArray = resources.obtainTypedArray(R.array.cover_name)
                        val coverName = nameArray.getString(data)
                        nameArray.recycle()
                        itemBinding.userCoverTv.text = coverName

                        itemBinding.userCoverIv.setOnClickListener {
                            callback.invoke(position)
                            dismiss()
                        }
                    }
                }
            }
        }

        activity.resources?.getIntArray(R.array.cover)?.let {
            val coverIndexArray = Array(it.size) { k -> k }
            binding.userCoverRv.setData(coverIndexArray.toMutableList())
        }

        setNegativeListener { dismiss() }

        setPositiveVisible(false)
    }
}