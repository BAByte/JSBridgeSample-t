package com.sample.mix.demo.jsbridge

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import com.sample.mix.demo.repository.data.response.JSResponse
import com.sample.mix.demo.utils.JsonUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object JSBridge : CoroutineScope by MainScope() {
    private const val TAG = ">>> NativeRepository"
    private var webViewClient: WebView? = null
    const val SYNC_TYPE = "sync.request"
    const val ASYNC_TYPE = "async.request"
    private const val SUB_TYPE = "sub."
    private const val RESPONSE_TYPE = "response"

    //js的异步请求处理器
    val asyncRequestMap = hashMapOf<String, Any>()

    //js的同步请求处理器
    val syncRequestMap = hashMapOf<String, Any>()

    //处理队列
    private val requestChannel = Channel<String>(Channel.BUFFERED)

    init {
        //触发JSBridgeConfig的init
        JSBridgeConfig
    }

    fun setWebClient(webViewClient: WebView?) {
        this.webViewClient = webViewClient
    }

    /**
     * 设置js回调方法的名称
     */
    fun setJSReceiveName(JSReceiveName: String) {
        JSBridgeConfig.CALL_BACK_NAME = JSReceiveName
    }

    /**
     * 区分异步和同步请求
     */
    private fun splitRequest(requestJson: String): String {
        val method = JSONObject(requestJson).getString("method")
        when {
            method.startsWith(SYNC_TYPE) -> {
                val response = (syncRequestMap[method] as IJSSyncRequest).onRequest(requestJson)
                response.method = changeType(response.method)
                return JsonUtils.toJson(response)
            }
            method.startsWith(ASYNC_TYPE) || method.startsWith(SUB_TYPE) -> {
                launch(Dispatchers.IO) {
                    try {
                        requestChannel.send(requestJson)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return ASYNC_TYPE
    }

    fun requestFromJS(requestJson: String): String {
        return splitRequest(requestJson)
    }

    /***
     * 处理js端过来的请求
     */
    fun startHandler() {
        launch(Dispatchers.IO) {
            for (requestJson in requestChannel) {
                try {
                    val method = JSONObject(requestJson).getString("method")
                    when {
                        method.startsWith(ASYNC_TYPE) -> {
                            val obj = asyncRequestMap[method] as IJSAsyncRequest
                            obj.onRequest(requestJson)
                        }
                        method.startsWith("sub") -> {
                            val obj = asyncRequestMap[method] as IJSSub
                            obj.onRequest(requestJson)
                        }
                        else -> {
                            Log.w(TAG, "Can't handler request for js : $requestJson")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun release() {
        asyncRequestMap.clear()
        syncRequestMap.clear()
        webViewClient = null
        cancel()
        requestChannel.close()
    }

    fun changeType(method: String): String {
        return when {
            method.startsWith(SYNC_TYPE) -> {
                method.replace(SYNC_TYPE, RESPONSE_TYPE)
            }
            method.startsWith(ASYNC_TYPE) -> {
                method.replace(ASYNC_TYPE, RESPONSE_TYPE)
            }
            else -> {
                method.replace(SYNC_TYPE, RESPONSE_TYPE)
            }
        }
    }

    fun <T> onResponse(response: JSResponse<T>, callBack: ((String) -> Unit)?) {
        response.method = changeType(response.method)
        val json = JsonUtils.toJson(response)
        launch(Dispatchers.Main) {
            webViewClient?.run {
                evaluateJavascript(
                    "${JSBridgeConfig.CALL_BACK_NAME}(\"${
                        handleJSFunctionParams(
                            json
                        )
                    }\")"
                ) {
                    callBack?.invoke(it)
                }
            } ?: run {
                callBack?.invoke("null")
            }
        }
    }

    /**
     * 处理传给JS函数的参数值。因为如果参数值带'、\n等特殊字符，JS会报错
     *
     * @param params 参数值
     * @return 已经处理过参数值
     */
    private fun handleJSFunctionParams(params: String): String {
        return params.replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }
}