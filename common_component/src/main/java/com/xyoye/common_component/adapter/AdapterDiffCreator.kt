package com.xyoye.common_component.adapter

import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.instanceParameter

/**
 * Created by xyoye on 2023/4/18
 */

class AdapterDiffCreator {

    //创建新数据
    private var newDataInstance: ((Any) -> Any)? = null

    //是否为同一个数据
    private var areItemsTheSame: ((Any, Any) -> Boolean)? = null

    //数据内容是否相同
    private var areContentsTheSame: ((Any, Any) -> Boolean)? = null

    fun newDataInstance(newDataInstance: (Any) -> Any) {
        this.newDataInstance = newDataInstance
    }

    fun areItemsTheSame(areItemsTheSame: (old: Any, new: Any) -> Boolean) {
        this.areItemsTheSame = areItemsTheSame
    }

    fun areContentsTheSame(areContentsTheSame: (old: Any, new: Any) -> Boolean) {
        this.areContentsTheSame = areContentsTheSame
    }

    fun createNewData(data: Any): Any {
        return newDataInstance?.invoke(data) ?: generateNewData(data)
    }

    fun isSameItem(oldData: Any, newData: Any): Boolean {
        if (oldData === newData) {
            return true
        }
        if (areItemsTheSame == null) {
            return oldData == newData
        }
        return areItemsTheSame!!.invoke(oldData, newData)
    }

    fun isSameContent(oldData: Any, newData: Any): Boolean {
        if (oldData === newData) {
            return true
        }
        if (areContentsTheSame == null) {
            return oldData == newData
        }
        return areContentsTheSame!!.invoke(oldData, newData)
    }

    private fun generateNewData(data: Any): Any {
        if (data === BaseAdapter.EMPTY_ITEM) {
            return data
        }
        if (data.javaClass.kotlin.isData) {
            return newInstance(data)
        }
        return data
    }

    private fun newInstance(any: Any): Any {
        // copy方法
        val copyFunc = any::class.declaredFunctions
            .find { it.name == "copy" }
            ?: return any

        // copy实例参数
        val instanceParameter = copyFunc.instanceParameter
            ?: return any

        // 调用data class copy方法，生成新实例
        // callBy方法只需传入实例参数，其它参数会使用默认值
        return copyFunc.callBy(mapOf(instanceParameter to any))
            ?: any
    }
}