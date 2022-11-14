package com.xyoye.player_component.widgets.popup

import android.graphics.Point

/**
 * Created by xyoye on 2022/11/11.
 */

interface PopupPositionListener {

    fun setPosition(point: Point)

    fun getPosition(): Point
}