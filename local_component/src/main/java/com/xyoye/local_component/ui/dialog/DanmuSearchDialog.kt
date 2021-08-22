package com.xyoye.local_component.ui.dialog

import androidx.core.view.isVisible
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.extension.decodeUrl
import com.xyoye.common_component.extension.toResString
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.KeywordHelper
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.DanmuSearchBean
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogSearchDanmuBinding

/**
 * Created by xyoye on 2020/11/26.
 */

class DanmuSearchDialog : BaseBottomDialog<DialogSearchDanmuBinding> {
    private var searchKeyword: String? = null
    private var videoName: String? = null
    private var tipsIndex = 1
    private lateinit var listener: (animeName: String, episodeId: String) -> Unit

    constructor() : super()

    constructor(
        searchKeyword: String? = null,
        videoName: String? = null,
        listener: (animeName: String, episodeId: String) -> Unit
    ) : super(true) {
        this.searchKeyword = searchKeyword
        this.videoName = videoName
        this.listener = listener
    }

    private lateinit var binding: DialogSearchDanmuBinding

    override fun getChildLayoutId() = R.layout.dialog_search_danmu

    override fun initView(binding: DialogSearchDanmuBinding) {
        this.binding = binding

        setTitle("搜索弹幕")

        initHistoryKeyword()

        initKeyword()

        if (!videoName.isNullOrEmpty()) {
            val tips = "$tipsIndex." + R.string.tips_danmu_search_file_name.toResString()
            binding.fileNameTips.text = tips

            binding.fileNameTips.isVisible = true
            binding.fileNameTv.isVisible = true
            binding.fileNameTv.setTextIsSelectable(true)
            binding.fileNameTv.text = videoName.decodeUrl()
        }

        binding.episodeRb.setOnClickListener {
            binding.episodeEt.isEnabled = true
            binding.ovaRb.isChecked = false
            binding.otherRb.isChecked = false
        }

        binding.ovaRb.setOnClickListener {
            binding.episodeEt.isEnabled = false
            binding.episodeRb.isChecked = false
            binding.otherRb.isChecked = false
        }

        binding.otherRb.setOnClickListener {
            binding.episodeEt.isEnabled = false
            binding.episodeRb.isChecked = false
            binding.ovaRb.isChecked = false
        }

        setNegativeListener { dismiss() }

        setPositiveListener {
            val animeName = binding.animeNameEt.text.toString()
            if (animeName.isEmpty()) {
                ToastCenter.showWarning("搜索番剧名不能为空")
                return@setPositiveListener
            }

            val episodeId = when {
                binding.episodeRb.isChecked -> {
                    val episodeStr = binding.episodeEt.text.toString()
                    if (episodeStr.isEmpty()) {
                        ToastCenter.showWarning("已选剧集类型，剧集不能为空")
                        return@setPositiveListener
                    }
                    episodeStr
                }
                binding.ovaRb.isChecked -> {
                    "movie"
                }
                else -> {
                    ""
                }
            }

            listener.invoke(animeName, episodeId)
            dismiss()
        }

        binding.animeNameEt.postDelayed({ showKeyboard(binding.animeNameEt) }, 200)
    }

    private fun initKeyword() {
        if (searchKeyword.isNullOrEmpty()) {
            return
        }
        val keywordList = KeywordHelper.extract(searchKeyword!!)
        if (keywordList.isEmpty()) {
            return
        }

        val tips = "$tipsIndex." + R.string.tips_danmu_search_keyword.toResString()
        binding.keywordLabelsTips.text = tips
        tipsIndex++

        binding.keywordLabelsView.isVisible = true
        binding.keywordLabelsTips.isVisible = true
        binding.keywordLabelsView.setLabels(keywordList)
        binding.keywordLabelsView.setOnLabelClickListener { _, data, _ ->
            if (!binding.animeNameEt.text.isNullOrEmpty()) {
                //已有文字，则先添加一个空格
                binding.animeNameEt.append(" ")
            }
            binding.animeNameEt.append(data as String)
        }
    }

    private fun initHistoryKeyword() {
        val lastDanmuSearchJson = AppConfig.getLastSearchDanmuJson()
        if (lastDanmuSearchJson.isNullOrEmpty())
            return

        val lastSearchBean = JsonHelper
            .parseJson<DanmuSearchBean>(lastDanmuSearchJson) ?: return

        val tips = "$tipsIndex." + R.string.tips_danmu_history_keyword.toResString()
        binding.keywordHistoryTips.text = tips
        tipsIndex++

        val displayText = "${lastSearchBean.animeName} ${lastSearchBean.episodeId}"
        binding.historyKeywordTv.text = displayText

        binding.keywordHistoryTips.isVisible = true
        binding.historyKeywordTv.isVisible = true
        binding.historyKeywordTv.setOnClickListener {
            listener.invoke(lastSearchBean.animeName, lastSearchBean.episodeId)
            dismiss()
        }
    }

    override fun dismiss() {
        if (this::binding.isInitialized) {
            hideKeyboard(binding.animeNameEt)
        }
        super.dismiss()
    }
}