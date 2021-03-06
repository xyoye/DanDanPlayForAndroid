package com.xyoye.player.controller.interfaces

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterViewController {

    fun addControlComponent(
        vararg controllerViews: InterControllerView,
        isIndependent: Boolean = false
    )

    fun removeControlComponent(controllerView: InterControllerView)

    fun removeAllControlComponent()

    fun removeAllIndependentComponents()
}