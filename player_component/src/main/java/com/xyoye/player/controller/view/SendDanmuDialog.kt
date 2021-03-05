package com.xyoye.player.controller.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.effective.android.panel.PanelSwitchHelper
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.weight.ColorSeekBar
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSendDanmuBinding

/**
 * Created by xyoye on 2021/2/21.
 */

class SendDanmuDialog(
    videoPosition: Long,
    private val mContext: Context,
    private val callback: (SendDanmuBean) -> Unit
) : Dialog(mContext) {

    private var danmuData = SendDanmuBean(videoPosition)

    private val viewBinding = DataBindingUtil.inflate<LayoutSendDanmuBinding>(
        LayoutInflater.from(context),
        R.layout.layout_send_danmu,
        null,
        false
    )

    private val mediumDanmuIv = viewBinding.danmuFontPanel.findViewById<ImageView>(R.id.medium_danmu_iv)
    private val smallDanmuIv = viewBinding.danmuFontPanel.findViewById<ImageView>(R.id.small_danmu_iv)

    private val scrollDanmuIv = viewBinding.danmuFontPanel.findViewById<ImageView>(R.id.scroll_danmu_iv)
    private val topDanmuIv = viewBinding.danmuFontPanel.findViewById<ImageView>(R.id.top_danmu_iv)
    private val bottomDanmuIv = viewBinding.danmuFontPanel.findViewById<ImageView>(R.id.bottom_danmu_iv)

    private val danmuColorSeekBar = viewBinding.danmuFontPanel.findViewById<ColorSeekBar>(R.id.danmu_color_seek_bar)

    init {
        setContentView(viewBinding.root)

        window?.let {
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setDimAmount(0f)
            it.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        }

        if (mContext is AppCompatActivity) {
            PanelSwitchHelper.Builder(mContext.window, viewBinding.root)
                .addPanelChangeListener {
                    onNone {
                        dismiss()
                    }

                    onKeyboard {
                        viewBinding.fontEditIv.isSelected = false
                    }

                    onPanel {
                        viewBinding.fontEditIv.isSelected = true
                        updatePanel()
                    }
                }.build(true)
        }

        initListener()
    }

    override fun show() {
        super.show()

        val layoutParams = window?.attributes ?: return
        layoutParams.apply {
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window?.apply {
            decorView.setPadding(0, 0, 0, 0)
            attributes = layoutParams
        }
    }

    override fun dismiss() {
        super.dismiss()

        if (mContext is AppCompatActivity) {
            ImmersionBar.with(mContext)
                .fullScreen(true)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init()
        }
    }

    private fun initListener() {
        setOnKeyListener { _, _, event ->
            if (event.action == KeyEvent.KEYCODE_BACK) {
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        viewBinding.danmuInputEt.addTextChangedListener(
            afterTextChanged = {
                viewBinding.sendDanmuIv.isSelected = it?.length != 0
            }
        )

        viewBinding.sendDanmuIv.setOnClickListener {
            val danmuText = viewBinding.danmuInputEt.text.toString().trim()
            if (danmuText.isEmpty()){
                ToastCenter.showOriginalToast("弹幕内容不能为空")
                return@setOnClickListener
            }

            danmuData.text = danmuText

            dismiss()
            callback.invoke(danmuData)
        }

        mediumDanmuIv.setOnClickListener {
            danmuData.isSmallSize = false
            updatePanel()
        }

        smallDanmuIv.setOnClickListener {
            danmuData.isSmallSize = true
            updatePanel()
        }

        scrollDanmuIv.setOnClickListener {
            danmuData.isScroll = true
            updatePanel()
        }

        topDanmuIv.setOnClickListener {
            danmuData.isScroll = false
            danmuData.isTop = true
            updatePanel()
        }

        bottomDanmuIv.setOnClickListener {
            danmuData.isScroll = false
            danmuData.isTop = false
            updatePanel()
        }

        danmuColorSeekBar.setOnColorChangeListener { _, color ->
            danmuData.color = color
        }
    }

    private fun updatePanel(){
        smallDanmuIv.isSelected = danmuData.isSmallSize
        mediumDanmuIv.isSelected = !danmuData.isSmallSize

        scrollDanmuIv.isSelected = danmuData.isScroll
        topDanmuIv.isSelected = !danmuData.isScroll && danmuData.isTop
        bottomDanmuIv.isSelected = !danmuData.isScroll && !danmuData.isTop
    }
}