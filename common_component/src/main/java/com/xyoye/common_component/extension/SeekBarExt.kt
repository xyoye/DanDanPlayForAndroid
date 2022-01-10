package com.xyoye.common_component.extension

import android.widget.SeekBar


/**
 * Created by xyoye on 2022/1/10
 */

inline fun SeekBar.observeProgressChange(crossinline block: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                block.invoke(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    })
}