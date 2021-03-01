package com.sample.mix.demo.repository.impl

import android.webkit.WebView
import com.sample.mix.demo.jsbridge.JSBridge
import com.sample.mix.demo.jsbridge.imp.sub.ListenNetworkStatus
import com.sample.mix.demo.repository.INativeRepository
import com.sample.mix.demo.repository.data.request.JSRequest
import com.sample.mix.demo.utils.JsonUtils


class NativeRepository : INativeRepository {

    override fun requestFromJS(requestJson: String): String {
        return JSBridge.requestFromJS(requestJson)
    }

    /**
     * 初始化
     */
    override fun initJSBridge(webViewClient: WebView?) {
        JSBridge.setWebClient(webViewClient)
        JSBridge.startHandler()
        val requestListenNetworkStatus = JSRequest(
            ListenNetworkStatus.method,
            "",
            ListenNetworkStatus.GetNetworkStatus(false)
        )
        requestFromJS(JsonUtils.toJson(requestListenNetworkStatus))
    }
}