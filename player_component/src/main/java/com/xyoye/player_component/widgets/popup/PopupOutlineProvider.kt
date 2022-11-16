package com.xyoye.player_component.widgets.popup

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Created by xyoye on 2022/11/16.
 */

class PopupOutlineProvider(private val radius: Float) : ViewOutlineProvider() {

    override fun getOutline(view: View?, outline: Outline?) {
        outline?.setRoundRect(
            0,
            0,
            view?.width ?: 0,
            view?.height ?: 0,
            radius
        )
    }
}