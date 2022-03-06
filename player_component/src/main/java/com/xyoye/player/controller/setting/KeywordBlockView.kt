package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutKeywordBlockBinding

/**
 * Created by xyoye on 2021/2/18.
 */

@SuppressLint("ClickableViewAccessibility")
class KeywordBlockView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutKeywordBlockBinding>(context, attrs, defStyleAttr) {

    private var addKeyword: ((keyword: String, isRegex: Boolean) -> Unit)? = null
    private var removeKeyword: ((id: Int) -> Unit)? = null

    init {
        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_keyword_block

    override fun getSettingViewType() = SettingViewType.KEYWORD_BLOCK

    override fun getGravity() = Gravity.START

    private fun initSettingListener() {
        viewBinding.keywordBlockLl.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        viewBinding.keywordLabelsView.setOnLabelLongClickListener { _, data, _ ->
            if (data is DanmuBlockEntity) {
                mControlWrapper.removeBlackList(data.isRegex, data.keyword)
                removeKeyword?.invoke(data.id)
            }
            return@setOnLabelLongClickListener true
        }

        viewBinding.keywordBlockAddTv.setOnClickListener {
            viewBinding.keywordBlockLl.requestFocus()

            var isRegex = false
            var newKeyword = viewBinding.keywordBlockAddEt.text.toString()

            if (newKeyword.isEmpty()) {
                ToastCenter.showOriginalToast("关键字不能为空")
                return@setOnClickListener
            }
            if ("regex=" == newKeyword) {
                ToastCenter.showOriginalToast("正则表达式内容不能为空")
                return@setOnClickListener
            }

            //是否为正则表达式
            if (newKeyword.startsWith("regex=")) {
                newKeyword = newKeyword.substring(6)
                isRegex = true
            }

            viewBinding.keywordBlockAddEt.setText("")
            mControlWrapper.addBlackList(isRegex, newKeyword)
            addKeyword?.invoke(newKeyword, isRegex)
        }
    }

    fun setDatabaseBlock(
        add: ((keyword: String, isRegex: Boolean) -> Unit),
        remove: ((id: Int) -> Unit),
        queryAll: () -> LiveData<MutableList<DanmuBlockEntity>>
    ) {
        addKeyword = add
        removeKeyword = remove
        queryAll.invoke().observe(context as LifecycleOwner) {
            viewBinding.keywordLabelsView.setLabels(it) { _, _, data -> data?.keyword ?: "" }

            //设置正则类型的标签为选中状态
            val regexPositionList = mutableListOf<Int>()
            for ((index, entity) in it.withIndex()) {
                if (entity.isRegex) {
                    regexPositionList.add(index)
                }
            }
            viewBinding.keywordLabelsView.setSelects(regexPositionList)

            //按正则和关键字屏蔽
            val regexList = mutableListOf<String>()
            val keywordList = mutableListOf<String>()
            it.forEach { entity ->
                if (entity.isRegex) {
                    regexList.add(entity.keyword)
                } else {
                    keywordList.add(entity.keyword)
                }
            }
            mControlWrapper.addBlackList(false, *keywordList.toTypedArray())
            mControlWrapper.addBlackList(true, *regexList.toTypedArray())
        }
    }
}