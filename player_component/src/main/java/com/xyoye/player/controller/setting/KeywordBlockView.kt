package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.ControlWrapper
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
) : ConstraintLayout(context, attrs, defStyleAttr), InterSettingView {
    private val mHideTranslateX = -dp2px(300).toFloat()

    private var addKeyword: ((keyword: String, isRegex: Boolean) -> Unit)? = null
    private var removeKeyword: ((id: Int) -> Unit)? = null

    private lateinit var mControlWrapper: ControlWrapper

    private val viewBinding = DataBindingUtil.inflate<LayoutKeywordBlockBinding>(
        LayoutInflater.from(context),
        R.layout.layout_keyword_block,
        this,
        true
    )

    init {
        viewBinding.apply {
            keywordBlockLl.setOnTouchListener { _, _ ->
                return@setOnTouchListener true
            }

            keywordLabelsView.setOnLabelLongClickListener { _, data, _ ->
                if (data is DanmuBlockEntity) {
                    mControlWrapper.removeBlackList(data.isRegex, data.keyword)
                    removeKeyword?.invoke(data.id)
                }
                return@setOnLabelLongClickListener true
            }

            keywordBlockAddTv.setOnClickListener {
                keywordBlockLl.requestFocus()

                var isRegex = false
                var newKeyword = keywordBlockAddEt.text.toString()

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

                keywordBlockAddEt.setText("")
                mControlWrapper.addBlackList(isRegex, newKeyword)
                addKeyword?.invoke(newKeyword, isRegex)
            }
        }
    }

    override fun getSettingViewType() = SettingViewType.KEYWORD_BLOCK

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.keywordBlockLl)
                .translationX(0f)
                .setDuration(500)
                .start()
        } else {
            ViewCompat.animate(viewBinding.keywordBlockLl)
                .translationX(mHideTranslateX)
                .setDuration(500)
                .start()
        }
    }

    override fun isSettingShowing() = viewBinding.keywordBlockLl.translationX == 0f

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

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