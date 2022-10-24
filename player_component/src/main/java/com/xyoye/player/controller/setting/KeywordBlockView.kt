package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutKeywordBlockBinding

/**
 * Created by xyoye on 2021/2/18.
 */

class KeywordBlockView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutKeywordBlockBinding>(context, attrs, defStyleAttr) {

    private var addKeyword: ((keyword: String, isRegex: Boolean) -> Unit)? = null
    private var removeKeyword: ((id: Int) -> Unit)? = null
    private val keywordList = mutableListOf<DanmuBlockEntity>()

    init {
        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_keyword_block

    override fun getSettingViewType() = SettingViewType.KEYWORD_BLOCK

    override fun getGravity() = Gravity.START

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        viewBinding.keywordBlockAddEt.requestFocus()
        return true
    }

    private fun initSettingListener() {
        viewBinding.keywordLabelsView.setOnLabelClickListener { _, data, _ ->
            if (data is DanmuBlockEntity) {
                mControlWrapper.removeBlackList(data.isRegex, data.keyword)
                removeKeyword?.invoke(data.id)
            }
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

        viewBinding.keywordBlockAddEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(viewBinding.keywordBlockAddEt)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
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
            keywordList.clear()
            keywordList.addAll(it)
            viewBinding.keywordLabelsView.setLabels(keywordList) { _, _, data ->
                data?.keyword ?: ""
            }

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

    private fun handleKeyCode(keyCode: Int): Boolean {
        if (viewBinding.keywordBlockAddEt.hasFocus()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                viewBinding.keywordBlockAddTv.requestFocus()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                val childCount = viewBinding.keywordLabelsView.childCount
                if (childCount > 0) {
                    viewBinding.keywordLabelsView.getChildAt(childCount - 1).requestFocus()
                }
            }
            return true
        }

        if (viewBinding.keywordBlockAddTv.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.keywordBlockAddEt.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.keywordLabelsView.getChildAt(0)
                    ?.requestFocus()
            }
            return true
        }

        return handleKeyLabelsView(keyCode)
    }

    private fun handleKeyLabelsView(keyCode: Int): Boolean {
        val focusedChild = viewBinding.keywordLabelsView.focusedChild
            ?: return false
        val position = viewBinding.keywordLabelsView.indexOfChild(focusedChild)
        if (position == -1) {
            return false
        }
        if (position == 0 && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
            viewBinding.keywordBlockAddTv.requestFocus()
            return true
        }
        if (position == viewBinding.keywordLabelsView.childCount - 1
            && (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        ) {
            viewBinding.keywordBlockAddEt.requestFocus()
            return true
        }

        val targetIndex = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> position - 1
            KeyEvent.KEYCODE_DPAD_RIGHT -> position + 1
            KeyEvent.KEYCODE_DPAD_UP -> position - 1
            KeyEvent.KEYCODE_DPAD_DOWN -> position + 1
            else -> position
        }
        viewBinding.keywordLabelsView.getChildAt(targetIndex)?.requestFocus()
        return true
    }
}