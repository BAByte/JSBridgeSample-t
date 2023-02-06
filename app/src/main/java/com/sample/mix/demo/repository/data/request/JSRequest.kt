package com.sample.mix.demo.repository.data.request

import androidx.annotation.Keep

@Keep
data class JSRequest<T>(
    /**
     * 用于区分前端调用native的方法类型
     * ex: request.setDeviceName
     */
    val method:String,

    /**
     * 用于区分请求
     */
    val requestId:String?,

    /**
     * 前端带过来的参数
     */
    val params:T
)