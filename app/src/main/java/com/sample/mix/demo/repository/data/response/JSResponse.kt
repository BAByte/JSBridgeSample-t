package com.sample.mix.demo.repository.data.response

import androidx.annotation.Keep

@Keep
data class JSResponse<T>(
    /**
     * 用于区分前端调用native的方法类型
     *
     * 回复前端
     * response.setDeviceName
     *
     * 主动通知前端网络状态
     * sub.connectStatus
     */
    var method:String,

    /**
     * 用于区分请求
     */
    val requestId:String?,

    val code:String,
    val message:String?,
    val data: T
)