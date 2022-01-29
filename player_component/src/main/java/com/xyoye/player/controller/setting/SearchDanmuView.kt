package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
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

    init {
        initRv()

        initListener()
    }

    override fun getLayoutId() = R.layout.layout_search_danmu

    override fun getSettingViewType() = SettingViewType.SEARCH_DANMU

    override fun getGravity() = Gravity.START

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
            viewBinding.danmuRv.setData(it)
        }
    }
}