package com.xyoye.stream_component.utils.ftp

/**
 * Created by xyoye on 2021/1/29.
 */

class FTPException : Exception {
    constructor() : super()

    constructor(msg: String?, throwable: Throwable) : super(msg, throwable)
}