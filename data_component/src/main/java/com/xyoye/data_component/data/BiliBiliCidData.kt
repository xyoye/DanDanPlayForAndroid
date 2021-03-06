package com.xyoye.data_component.data

/**
 * Created by xyoye on 2021/2/23.
 */

data class BiliBiliCidData(
    val code: Int,
    val data: CidData?
)

data class CidData(
    val title: String?,
    val cid: Int
)