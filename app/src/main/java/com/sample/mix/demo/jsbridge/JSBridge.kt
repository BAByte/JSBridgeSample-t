package com.sample.mix.demo.jsbridge

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import com.google.gson.Gson
import com.sample.mix.demo.repository.data.request.JSRequest
import com.sample.mix.demo.repository.data.response.JSResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object JSBridge : CoroutineScope by MainScope() {
    private const val TAG = ">>> JSBridge"
    private var webViewClient: WebView? = null
    const val SYNC_TYPE = "sync.request"
    const val ASYNC_TYPE = "async.request"
    const val SUB_TYPE = "sub"
    private const val RESPONSE_TYPE = "response"
    private val gson = Gson()

    //js的异步请求处理器
    val asyncRequestMap = hashMapOf<String, Any>()

    //js的同步请求处理器
    val syncRequestMap = hashMapOf<String, Any>()

    //js的异步请求处理器
    val asyncResponseMap = hashMapOf<String, IJSResponse>()

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
                return gson.toJson(response)
            }
            else -> {
                launch(Dispatchers.IO) {
                    try {
                        Log.d(TAG, ">>> send request=$requestJson")
                        requestChannel.send(requestJson)
                    } catch (e: Exception) {
                        Log.d(TAG, ">>> splitRequest ${e.stackTraceToString()}")
                    }
                }
            }
        }
        return ASYNC_TYPE
    }

    fun requestFromJS(requestJson: String): String {
        Log.d(TAG, ">>> requestJson = $requestJson")
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
                        method.startsWith(RESPONSE_TYPE) -> {
                            responseFromJs(requestJson)
                        }
                        method.startsWith(ASYNC_TYPE) -> {
                            //给js的回复
                            val obj = asyncRequestMap[method] as IJSAsyncRequest
                            obj.onRequest(requestJson)
                        }
                        method.startsWith(SUB_TYPE) -> {
                            val obj = asyncRequestMap[method] as IJSSub
                            obj.onEvent(requestJson)
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

    fun responseFromJs(json: String) {
        Log.d(TAG, "responseFromJs json  = $json")
        val jsonObject = JSONObject(json)
        val realMethod = getResponseMethod(jsonObject)
        Log.d(TAG, "responseFromJs realMethod  = $realMethod")

        val obj = asyncResponseMap[realMethod] as IJSResponse
        unRegisterJsResponse(realMethod)
        obj.onCallBack(json)
    }

    private fun <T> registerJsResponse(request: JSRequest<T>, ijsResponse: IJSResponse) {
        val realMethod = getResponseMethod(request)
        Log.d(TAG, "registerJsResponse realMethod  = $realMethod")
        asyncResponseMap[realMethod] = ijsResponse
    }

    private fun <T> unRegisterJsResponse(request: JSRequest<T>) {
        val realMethod = getResponseMethod(request)
        Log.d(TAG, "unRegisterJsResponse realMethod  = $realMethod")
        asyncResponseMap.remove(realMethod)
    }

    private fun unRegisterJsResponse(realMethod: String) {
        Log.d(TAG, "unRegisterJsResponse realMethod  = $realMethod")
        asyncResponseMap.remove(realMethod)
    }

    /**
     * js回复的时候，为了找到唯一回调，需要重组method
     */
    fun getResponseMethod(jsonObject: JSONObject): String {
        val method = jsonObject.getString("method")
        val requestId = jsonObject.getString("requestId")
        return "$method#$requestId"
    }

    /**
     * js回复的时候，为了找到唯一回调，需要重组method
     */
    fun <T> getResponseMethod(jsRequest: JSRequest<T>): String {
        val method = changeType(jsRequest.method)
        val requestId = jsRequest.requestId
        return "$method#$requestId"
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
                method
            }
        }
    }

    fun <T> publish2JS(response: JSResponse<T>, callBack: ((String) -> Unit)?) {
        toJS(gson.toJson(response), callBack)
    }

    fun <T> onResponse(response: JSResponse<T>, callBack: ((String) -> Unit)?) {
        response.method = changeType(response.method)
        toJS(gson.toJson(response), callBack)
    }

    fun <T> sendRequest(request: JSRequest<T>, ijsResponse: IJSResponse) {
        val requestJson = gson.toJson(request)
        Log.d(TAG, "sendRequest requestJson = $requestJson")
        //注册一个临时回调
        registerJsResponse(request, ijsResponse)
        toJS(requestJson) { response ->
            Log.d(TAG, "response  = $response")
            //失败了，web没有初始化成功，直接回调
            if (!isEffectiveResult(response)) {
                unRegisterJsResponse(request)
                ijsResponse.onCallBack(null)
            }
        }
    }

    fun toJS(json: String, callBack: ((String) -> Unit)?) {
        Log.d(TAG, "toJS json = $json")
        launch(Dispatchers.Main) {
            webViewClient?.run {
                evaluateJavascript(
                    "${JSBridgeConfig.CALL_BACK_NAME}(\"${
                        handleJSFunctionParams(
                            json
                        )
                    }\")"
                ) {
                    Log.d(TAG, "toJS evaluateJavascript result = $it")
                    callBack?.invoke(it)
                }
            } ?: run {
                callBack?.invoke(JSBridgeConfig.WEB_NOT_INIT_CALLBACK_MS)
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

    fun isEffectiveResult(res: String): Boolean {
        return !TextUtils.isEmpty(res) && res != JSBridgeConfig.WEB_NOT_INIT_CALLBACK_MS
    }

}