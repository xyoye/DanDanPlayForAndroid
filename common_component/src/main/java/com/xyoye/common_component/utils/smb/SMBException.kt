package com.xyoye.common_component.utils.smb

/**
 * Created by xyoye on 2021/2/3.
 */

class SMBException : Exception {
    constructor() : super()

    constructor(msg: String?, throwable: Throwable) : super(msg, throwable)
}