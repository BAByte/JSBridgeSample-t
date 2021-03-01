package com.sample.mix.demo.jsbridge

import com.sample.mix.demo.jsbridge.imp.request.async.ToSetNetwork
import com.sample.mix.demo.jsbridge.imp.request.sync.GetVersionCode
import com.sample.mix.demo.jsbridge.imp.sub.ListenNetworkStatus

/**
 * 建立method与处理器的关系
 */
object JSBridgeConfig {
    //状态码
    const val SUCCESS_CODE = "000000"
    const val UNKNOWN_ERROR_CODE = "999999"

    //js回调名称
    var CALL_BACK_NAME = "JSBridgeReceiveMessage"

    init {
        with(JSBridge) {
            //异步
            //sub
            asyncRequestMap[ListenNetworkStatus.method] = ListenNetworkStatus()

            //request
            asyncRequestMap["$ASYNC_TYPE.toSetNetwork"] = ToSetNetwork()

            //同步
            syncRequestMap["$SYNC_TYPE.getVersionCode"] = GetVersionCode()
        }
    }
}