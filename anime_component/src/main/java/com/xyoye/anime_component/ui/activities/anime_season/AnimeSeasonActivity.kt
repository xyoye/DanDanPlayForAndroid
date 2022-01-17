package com.xyoye.anime_component.ui.activities.anime_season

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityAnimeSeasonBinding
import com.xyoye.anime_component.databinding.ItemCommonScreenBinding
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.anime_component.ui.adapter.setNewAnimeData
import com.xyoye.anime_component.ui.dialog.date_picker.DatePickerDialog
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.data.CommonTypeData

@Route(path = RouteTable.Anime.AnimeSeason)
class AnimeSeasonActivity : BaseActivity<AnimeSeasonViewModel, ActivityAnimeSeasonBinding>() {
    private val animeAdapter = AnimeAdapter.getAdapter(this)

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeSeasonViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_anime_season

    override fun initView() {

        title = "季度番剧"

        initObserver()

        initRv()

        viewModel.getYearsData()
    }

    private fun initRv() {
        dataBinding.yearRv.run {
            layoutManager = grid(4)

            adapter = buildAdapter {
                addItem<CommonTypeData, ItemCommonScreenBinding>(R.layout.item_common_screen) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            typeNameTv.text = data.typeName
                            typeNameTv.setTextColorRes(if (data.isChecked) R.color.text_theme else R.color.text_black)
                            itemLayout.setOnClickListener {
                                if (data.isEnable) {
                                    viewModel.checkYear(data.typeId)
                                } else {
                                    DatePickerDialog(data.typeId.toInt()) {
                                        viewModel.checkYear(it.toString())
                                    }.show(this@AnimeSeasonActivity)
                                }
                            }
                        }
                    }
                }
            }
        }

        dataBinding.seasonRv.apply {
            layoutManager = grid(4)

            adapter = buildAdapter {
                addItem<CommonTypeData, ItemCommonScreenBinding>(R.layout.item_common_screen) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            typeNameTv.text = data.typeName
                            typeNameTv.setTextColorRes(
                                when {
                                    !data.isEnable -> R.color.text_gray
                                    data.isChecked -> R.color.text_theme
                                    else -> R.color.text_black
                                }
                            )
                            itemLayout.setOnClickListener {
                                if (data.isEnable) {
                                    viewModel.checkSeason(data.typeId)
                                }
                            }
                        }
                    }
                }
            }
        }

        dataBinding.sortRv.apply {
            layoutManager = grid(4)

            adapter = buildAdapter {
                initData(viewModel.sortTypeData)

                addItemDecoration(ItemDecorationSpace(dp2px(2), 0))

                addItem<CommonTypeData, ItemCommonScreenBinding>(R.layout.item_common_screen) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            typeNameTv.text = data.typeName
                            typeNameTv.setTextColorRes(if (data.isChecked) R.color.text_theme else R.color.text_black)
                            itemLayout.setOnClickListener {
                                viewModel.checkSort(position)
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
        }

        dataBinding.animeRv.apply {

            layoutManager = gridEmpty(3)

            adapter = animeAdapter

            val pxValue = dp2px(10)
            addItemDecoration(
                ItemDecorationDrawable(
                    pxValue,
                    pxValue,
                    R.color.item_bg_color.toResColor()
                )
            )
        }
    }

    private fun initObserver() {
        viewModel.yearsLiveData.observe(this) {
            dataBinding.yearRv.setData(it)
        }

        viewModel.seasonLiveData.observe(this) {
            dataBinding.seasonRv.setData(it)
        }

        viewModel.animeLiveData.observe(this) {
            //保留recycler view位置，避免滚动
            val recyclerSaveState = dataBinding.animeRv.layoutManager?.onSaveInstanceState()
            animeAdapter.setNewAnimeData(it)
            dataBinding.animeRv.layoutManager?.onRestoreInstanceState(recyclerSaveState)
        }

        viewModel.animeSortUpdateLiveData.observe(this) {
            dataBinding.sortRv.adapter?.notifyItemChanged(it)
        }
    }
}