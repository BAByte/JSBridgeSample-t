package com.sample.mix.demo.jsbridge.imp.request.async

import android.content.Intent
import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.sample.mix.demo.App
import com.sample.mix.demo.jsbridge.IJSAsyncRequest
import com.sample.mix.demo.jsbridge.JSBridge
import com.sample.mix.demo.jsbridge.JSBridgeConfig
import com.sample.mix.demo.repository.data.request.JSRequest
import com.sample.mix.demo.repository.data.request.NoneRequest
import com.sample.mix.demo.repository.data.response.JSResponse
import com.sample.mix.demo.repository.data.response.NoneResponse
import com.sample.mix.demo.utils.JsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 打开设置：
 * 获取参数 这里的例子是没有参数，如果需要：请给JSRequest或JSResponse传入对应的data class
 */
class ToSetNetwork : IJSAsyncRequest, CoroutineScope by MainScope() {
    override fun onRequest(params: String) {
        launch(Dispatchers.Main) {
            var request: JSRequest<NoneRequest>? = null
            try {
                val type = object : TypeToken<JSRequest<NoneRequest>>() {}.type
                request = JsonUtils.fromJson(params, type)
                if (request == null) {
                    return@launch
                }
                App.ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                val response = JSResponse(
                    request.method,
                    request.requestId,
                    JSBridgeConfig.SUCCESS_CODE,
                    "",
                    NoneResponse("")
                )
                JSBridge.onResponse(response, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}