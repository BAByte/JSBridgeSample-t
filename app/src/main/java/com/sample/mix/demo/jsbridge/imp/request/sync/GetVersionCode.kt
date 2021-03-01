package com.sample.mix.demo.jsbridge.imp.request.sync

import androidx.annotation.Keep
import com.google.gson.reflect.TypeToken
import com.sample.mix.demo.jsbridge.IJSSyncRequest
import com.sample.mix.demo.jsbridge.JSBridgeConfig.SUCCESS_CODE
import com.sample.mix.demo.repository.data.request.JSRequest
import com.sample.mix.demo.repository.data.request.NoneRequest
import com.sample.mix.demo.repository.data.response.JSResponse
import com.sample.mix.demo.utils.JsonUtils

/**
 * 获取版本号
 */
class GetVersionCode : IJSSyncRequest {

    @Keep
    private data class VersionCode(val versionCode: String)

    override fun onRequest(params: String): JSResponse<Any> {
        val type = object : TypeToken<JSRequest<NoneRequest>>() {}.type
        val request: JSRequest<NoneRequest> = JsonUtils.fromJson(params, type)
        return JSResponse(
            request.method,
            request.requestId,
            SUCCESS_CODE,
            "",
            VersionCode("1.1") as Any
        )
    }
}