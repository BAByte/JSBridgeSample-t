package com.sample.mix.demo.repository.impl

import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sample.mix.demo.jsbridge.IJSResponse
import com.sample.mix.demo.jsbridge.JSBridge
import com.sample.mix.demo.jsbridge.JSBridgeConfig
import com.sample.mix.demo.jsbridge.imp.sub.ListenNetworkStatus
import com.sample.mix.demo.repository.INativeRepository
import com.sample.mix.demo.repository.data.request.JSRequest
import com.sample.mix.demo.repository.data.response.JSResponse
import com.sample.mix.demo.utils.JsonUtils
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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
        //这里模拟前端订阅网络
        val requestListenNetworkStatus = JSRequest(
            ListenNetworkStatus.method,
            null,
            ListenNetworkStatus.GetNetworkStatus(false)
        )
        requestFromJS(JsonUtils.toJson(requestListenNetworkStatus))
    }


    /**
     * 当native端需要获取JS的数据时，可以看这个例子
     * todo 注：这里你可以自己加个超时机制
     */
    override suspend fun getUserNameFromJS(id: String): String = suspendCoroutine {
        val request = JSRequest(
            "和JS约定的method",
            UUID.randomUUID().toString(),
            null
        )

        JSBridge.sendRequest(request, object : IJSResponse {
            override fun onCallBack(response: String?) {
                Log.d("NativeRepository", "getDataFromJS response = $response")
                if (response == null) {
                    it.resume("你定义的一些错误信息")
                    return
                }
                val type = object : TypeToken<JSResponse<Void>>() {}.type
                val responseObj: JSResponse<String> = Gson().fromJson(response, type)
                if (responseObj.code != JSBridgeConfig.SUCCESS_CODE) {
                    it.resume("你定义的一些错误信息")
                    return
                }
                it.resume(responseObj.data)
            }
        })
    }
}