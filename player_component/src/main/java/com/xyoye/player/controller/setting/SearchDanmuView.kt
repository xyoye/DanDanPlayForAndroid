package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemSearchDanmuBinding
import com.xyoye.player_component.databinding.LayoutSearchDanmuBinding

class SearchDanmuView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSearchDanmuBinding>(context, attrs, defStyleAttr) {

    private var search: ((String) -> Unit)? = null
    private var download: ((DanmuSourceContentBean) -> Unit)? = null
    private val mSearchDanmuData = mutableListOf<DanmuSourceContentBean>()

    init {
        initRv()

        initListener()
    }

    override fun getLayoutId() = R.layout.layout_search_danmu

    override fun getSettingViewType() = SettingViewType.SEARCH_DANMU

    override fun getGravity() = Gravity.START


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return true
        }

        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        viewBinding.tvSelectLocalDanmu.requestFocus()
        return true
    }

    private fun initRv() {
        viewBinding.danmuRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<DanmuSourceContentBean, ItemSearchDanmuBinding>(R.layout.item_search_danmu) {
                    initView { data, position, _ ->
                        val positionText = (position + 1).toString()
                        itemBinding.positionTv.text = positionText
                        itemBinding.episodeTv.text = data.episodeTitle
                        itemBinding.animeTv.text = data.animeTitle
                        itemBinding.itemLayout.setOnClickListener {
                            download(data)
                        }
                    }
                }
            }
        }
    }

    private fun initListener() {
        viewBinding.tvSelectLocalDanmu.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.LOAD_DANMU_SOURCE)
            onSettingVisibilityChanged(false)
        }

        viewBinding.searchDanmuEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.searchDanmuTv.setOnClickListener {
            search()
        }
    }

    private fun search() {
        hideKeyboard(viewBinding.searchDanmuEt)
        viewBinding.searchDanmuCl.requestFocus()
        val searchText = viewBinding.searchDanmuEt.text.toString().trim()
        search?.invoke(searchText)
    }

    private fun download(data: DanmuSourceContentBean) {
        download?.invoke(data)
        onSettingVisibilityChanged(false)
    }

    fun setDanmuSearch(
        search: (String) -> Unit,
        download: (DanmuSourceContentBean) -> Unit,
        searchResult: () -> LiveData<List<DanmuSourceContentBean>>
    ) {
        this.search = search
        this.download = download
        searchResult.invoke().observe(context as LifecycleOwner) {
            mSearchDanmuData.clear()
            mSearchDanmuData.addAll(it)
            viewBinding.danmuRv.setData(mSearchDanmuData)
        }
    }

    /**
     * 根据KeyCode目标焦点ItemBinding
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        if (viewBinding.tvSelectLocalDanmu.hasFocus()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                viewBinding.searchDanmuEt.requestFocus()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (mSearchDanmuData.size > 0) {
                    viewBinding.danmuRv.requestIndexChildFocus(0)
                } else {
                    viewBinding.searchDanmuEt.requestFocus()
                }
            }
            return true
        }

        if (viewBinding.searchDanmuEt.hasFocus()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                viewBinding.searchDanmuTv.requestFocus()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                viewBinding.tvSelectLocalDanmu.requestFocus()
            }
            return true
        }

        if (viewBinding.searchDanmuTv.hasFocus()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                viewBinding.searchDanmuEt.requestFocus()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                viewBinding.tvSelectLocalDanmu.requestFocus()
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mSearchDanmuData.size > 0) {
                    viewBinding.danmuRv.requestIndexChildFocus(0)
                } else {
                    viewBinding.searchDanmuEt.requestFocus()
                }
            }
            return true
        }

        return handleKeyCodeInResultRv(keyCode)
    }

    private fun handleKeyCodeInResultRv(keyCode: Int): Boolean {
        //已获取的焦点View
        val focusedChild = viewBinding.danmuRv.focusedChild
            ?: return false
        val focusedChildIndex = viewBinding.danmuRv.getChildAdapterPosition(focusedChild)
        //已获取的焦点View的位置
        if (focusedChildIndex == -1) {
            return false
        }

        //向左或向右的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            focusedChild.requestFocus()
            return true
        }

        //向上的事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            val index = mSearchDanmuData.findIndexOnLeft(focusedChildIndex) { true }
            if (index != -1) {
                viewBinding.danmuRv.requestIndexChildFocus(index)
                return true
            }

            viewBinding.searchDanmuTv.requestFocus()
            return true
        }

        //向下的事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val index = mSearchDanmuData.findIndexOnRight(focusedChildIndex) { true }
            if (index != -1) {
                viewBinding.danmuRv.requestIndexChildFocus(index)
                return true
            }

            viewBinding.tvSelectLocalDanmu.requestFocus()
            return true
        }

        return false
    }
}