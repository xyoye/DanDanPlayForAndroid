package com.xyoye.player.controller.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.controller.interfaces.InterSettingView
import com.xyoye.player.controller.wrapper.ControlWrapper
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuBinding

/**
 * Created by xyoye on 2020/11/20.
 */

class SettingDanmuView(
    context: Context,
    private val danmuView: DanmuView,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterSettingView {
    private val mHideTranslateX = dp2px(300).toFloat()

    private lateinit var mControlWrapper: ControlWrapper

    private val viewBinding = DataBindingUtil.inflate<LayoutSettingDanmuBinding>(
        LayoutInflater.from(context),
        R.layout.layout_setting_danmu,
        this,
        true
    )

    init {
        gravity = Gravity.END

        initSettingView()

        initSettingListener()
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getSettingViewType() = SettingViewType.DANMU_SETTING

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.danmuSourceTv.text = danmuView.mUrl
            ViewCompat.animate(viewBinding.playerSettingNsv).translationX(0f).setDuration(500)
                .start()
        } else {
            ViewCompat.animate(viewBinding.playerSettingNsv).translationX(mHideTranslateX)
                .setDuration(500)
                .start()
        }
    }

    override fun isSettingShowing() = viewBinding.playerSettingNsv.translationX == 0f

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

    private fun initSettingView() {
        //文字大小
        val danmuSizePercent = PlayerInitializer.Danmu.size
        val danmuSizeText = "$danmuSizePercent%"
        viewBinding.danmuSizeTv.text = danmuSizeText
        viewBinding.danmuSizeSb.progress = danmuSizePercent

        //弹幕速度
        val danmuSpeedPercent = PlayerInitializer.Danmu.speed
        val danmuSpeedText = "$danmuSpeedPercent%"
        viewBinding.danmuSpeedTv.text = danmuSpeedText
        viewBinding.danmuSpeedSb.progress = danmuSpeedPercent

        //弹幕透明度
        val danmuAlphaPercent = PlayerInitializer.Danmu.alpha
        val danmuAlphaText = "$danmuAlphaPercent%"
        viewBinding.danmuAlphaTv.text = danmuAlphaText
        viewBinding.danmuAlphaSb.progress = danmuAlphaPercent

        //弹幕描边宽度
        val danmuStokePercent = PlayerInitializer.Danmu.stoke
        val danmuStokeText = "$danmuStokePercent%"
        viewBinding.danmuStokeTv.text = danmuStokeText
        viewBinding.danmuStokeSb.progress = danmuStokePercent

        //弹幕时间调节
        val extraPosition = PlayerInitializer.Danmu.offsetPosition / 1000f
        viewBinding.danmuExtraTimeEt.setText(extraPosition.toString())

        //弹幕类型屏蔽
        viewBinding.mobileDanmuIv.isSelected = !PlayerInitializer.Danmu.mobileDanmu
        viewBinding.topDanmuIv.isSelected = !PlayerInitializer.Danmu.topDanmu
        viewBinding.bottomDanmuIv.isSelected = !PlayerInitializer.Danmu.bottomDanmu

        //滚动弹幕行数限制
        updateMaxDanmuLine()

        //弹幕同屏数量限制
        updateMaxDanmuNum()
    }

    private fun initSettingListener() {
        viewBinding.danmuSizeSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.danmuSizeTv.text = progressText

                DanmuConfig.putDanmuSize(progress)
                PlayerInitializer.Danmu.size = progress
                danmuView.updateDanmuSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.danmuSpeedSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.danmuSpeedTv.text = progressText

                DanmuConfig.putDanmuSpeed(progress)
                PlayerInitializer.Danmu.speed = progress
                danmuView.updateDanmuSpeed()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.danmuAlphaSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.danmuAlphaTv.text = progressText

                DanmuConfig.putDanmuAlpha(progress)
                PlayerInitializer.Danmu.alpha = progress
                danmuView.updateDanmuAlpha()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.danmuStokeSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressText = "$progress%"
                viewBinding.danmuStokeTv.text = progressText

                DanmuConfig.putDanmuStoke(progress)
                PlayerInitializer.Danmu.stoke = progress
                danmuView.updateDanmuStoke()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.switchDanmuTv.setOnClickListener {
            onSettingVisibilityChanged(false)
            mControlWrapper.switchDanmuSource()
        }

        viewBinding.danmuExtraTimeReduce.setOnClickListener {
            hideKeyboard(viewBinding.danmuExtraTimeEt)
            viewBinding.danmuOffsetTimeLl.requestFocus()
            PlayerInitializer.Danmu.offsetPosition -= 500
            updateOffsetEt()
            danmuView.updateOffsetTime()
        }

        viewBinding.danmuExtraTimeAdd.setOnClickListener {
            hideKeyboard(viewBinding.danmuExtraTimeEt)
            viewBinding.danmuOffsetTimeLl.requestFocus()
            PlayerInitializer.Danmu.offsetPosition += 500
            updateOffsetEt()
            danmuView.updateOffsetTime()
        }

        viewBinding.danmuExtraTimeEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(viewBinding.danmuExtraTimeEt)
                viewBinding.danmuOffsetTimeLl.requestFocus()

                val extraTimeText = viewBinding.danmuExtraTimeEt.text.toString()
                val newOffsetSecond = if (extraTimeText.isEmpty()) 0f else extraTimeText.toFloat()

                PlayerInitializer.Danmu.offsetPosition = (newOffsetSecond * 1000).toLong()
                updateOffsetEt()
                danmuView.updateOffsetTime()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.mobileDanmuIv.setOnClickListener {
            val newState = !PlayerInitializer.Danmu.mobileDanmu
            viewBinding.mobileDanmuIv.isSelected = !newState
            PlayerInitializer.Danmu.mobileDanmu = newState
            DanmuConfig.putShowMobileDanmu(newState)
            danmuView.updateMobileDanmuState()
        }

        viewBinding.topDanmuIv.setOnClickListener {
            val newState = !PlayerInitializer.Danmu.topDanmu
            viewBinding.topDanmuIv.isSelected = !newState
            PlayerInitializer.Danmu.topDanmu = newState
            DanmuConfig.putShowTopDanmu(newState)
            danmuView.updateTopDanmuState()
        }

        viewBinding.bottomDanmuIv.setOnClickListener {
            val newState = !PlayerInitializer.Danmu.bottomDanmu
            viewBinding.bottomDanmuIv.isSelected = !newState
            PlayerInitializer.Danmu.bottomDanmu = newState
            DanmuConfig.putShowBottomDanmu(newState)
            danmuView.updateBottomDanmuState()
        }

        viewBinding.maxLineTv.setOnClickListener {
            hideKeyboard(viewBinding.maxLineEt)
            viewBinding.maxLineLl.requestFocus()
            PlayerInitializer.Danmu.maxLine = -1
            DanmuConfig.putDanmuMaxLine(-1)
            updateMaxDanmuLine()
            danmuView.updateMaxLine()
        }

        viewBinding.maxLineEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(viewBinding.maxLineEt)
                viewBinding.maxLineLl.requestFocus()

                //输入为空或0，设置为五限制
                val maxLineText = viewBinding.maxLineEt.text.toString()
                var newMaxLine = if (maxLineText.isEmpty()) -1 else maxLineText.toInt()
                newMaxLine = if (newMaxLine == 0) -1 else newMaxLine

                PlayerInitializer.Danmu.maxLine = newMaxLine
                DanmuConfig.putDanmuMaxLine(newMaxLine)
                updateMaxDanmuLine()
                danmuView.updateMaxLine()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.numberNoLimitTv.setOnClickListener {
            viewBinding.numberLimitRl.requestFocus()
            hideKeyboard(viewBinding.numberInputLimitEt)
            PlayerInitializer.Danmu.maxNum = 0
            DanmuConfig.putDanmuMaxCount(0)
            updateMaxDanmuNum()
            danmuView.updateMaxScreenNum()
        }

        viewBinding.numberAutoLimitTv.setOnClickListener {
            viewBinding.numberLimitRl.requestFocus()
            hideKeyboard(viewBinding.numberInputLimitEt)
            PlayerInitializer.Danmu.maxNum = -1
            DanmuConfig.putDanmuMaxCount(-1)
            updateMaxDanmuNum()
            danmuView.updateMaxScreenNum()
        }

        viewBinding.numberInputLimitEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewBinding.numberLimitRl.requestFocus()
                hideKeyboard(viewBinding.numberInputLimitEt)

                //输入为空，设置为五限制
                val maxNumText = viewBinding.numberInputLimitEt.text.toString()
                val newMaxNum = if (maxNumText.isEmpty()) 0 else maxNumText.toInt()

                PlayerInitializer.Danmu.maxNum = newMaxNum
                DanmuConfig.putDanmuMaxCount(newMaxNum)
                updateMaxDanmuNum()
                danmuView.updateMaxScreenNum()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.keywordBlockTv.setOnClickListener {
            onSettingVisibilityChanged(false)
            mControlWrapper.showSettingView(SettingViewType.KEYWORD_BLOCK)
        }
    }

    private fun updateOffsetEt() {
        val offsetSecond = PlayerInitializer.Danmu.offsetPosition / 1000f
        viewBinding.danmuExtraTimeEt.setText(offsetSecond.toString())
    }

    private fun updateMaxDanmuLine() {
        if (PlayerInitializer.Danmu.maxLine == -1) {
            viewBinding.maxLineTv.isSelected = true
            viewBinding.maxLineEt.setText("")
        } else {
            viewBinding.maxLineTv.isSelected = false
            viewBinding.maxLineEt.setText(PlayerInitializer.Danmu.maxLine.toString())
        }
    }

    private fun updateMaxDanmuNum() {
        when (PlayerInitializer.Danmu.maxNum) {
            -1 -> {
                viewBinding.numberNoLimitTv.isSelected = false
                viewBinding.numberAutoLimitTv.isSelected = true
                viewBinding.numberInputLimitEt.setText("")
            }
            0 -> {
                viewBinding.numberNoLimitTv.isSelected = true
                viewBinding.numberAutoLimitTv.isSelected = false
                viewBinding.numberInputLimitEt.setText("")
            }
            else -> {
                viewBinding.numberNoLimitTv.isSelected = false
                viewBinding.numberAutoLimitTv.isSelected = false
                viewBinding.numberInputLimitEt.setText(PlayerInitializer.Danmu.maxNum.toString())
            }
        }

    }
}