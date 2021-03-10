package com.sample.mix.demo.jsbridge.imp.sub

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.text.TextUtils
import androidx.annotation.Keep
import com.sample.mix.demo.App
import com.sample.mix.demo.jsbridge.IJSSub
import com.sample.mix.demo.jsbridge.JSBridge
import com.sample.mix.demo.jsbridge.JSBridgeConfig.SUCCESS_CODE
import com.sample.mix.demo.repository.data.response.JSResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
* 监听网络状态
*/
class ListenNetworkStatus : IJSSub {
    companion object {
        const val method = "sub.listenNetworkStatus"
    }

    @Keep
    data class GetNetworkStatus(val connect: Boolean)

    override fun onRequest(params: String) {
        //安卓7以上可用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            publishFirst()
        }
    }

    private fun publishFirst() {
        val connectivityManager =
            App.ctx.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiInfo: NetworkInfo =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val ethInfo: NetworkInfo =
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
        val isConnect = wifiInfo.isConnected || ethInfo.isConnected
        val isAvailable = wifiInfo.isAvailable || ethInfo.isAvailable
        //如果是断网启动，先上报一次网络状态
        val responseFirst = JSResponse(
            method,
            "",
            SUCCESS_CODE,
            "",
            GetNetworkStatus(isConnect && isAvailable && canBrowseBaidu())
        )
        Logger.d(">>> NativeRepository ListenNetworkStatus publishFirst responseFirst = $responseFirst")
        JSBridge.onResponse(responseFirst) { value ->
            Logger.d(">>> NativeRepository ListenNetworkStatus publishFirst callback = $value")
            if (TextUtils.isEmpty(value) || value == "null") {
                GlobalScope.launch(Dispatchers.IO) {
                    delay(1000L)
                    publishFirst()
                }
            } else {
                registerCallBack()
            }
        }
    }

    private fun registerCallBack() {
        val connectivityManager =
            App.ctx.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val response = JSResponse(
                        method,
                        "",
                        SUCCESS_CODE,
                        "",
                        GetNetworkStatus(canBrowseBaidu())
                    )
                    Logger.d(">>> NativeRepository ListenNetworkStatus onAvailable response = $response")
                    JSBridge.onResponse(response, null)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    val response = JSResponse(
                        method,
                        "",
                        SUCCESS_CODE,
                        "",
                        GetNetworkStatus(canBrowseBaidu())
                    )
                    Logger.d(">>> NativeRepository ListenNetworkStatus onLost response = $response")
                    JSBridge.onResponse(response, null)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    val response = JSResponse(
                        method,
                        "",
                        SUCCESS_CODE,
                        "",
                        GetNetworkStatus(false)
                    )
                    Logger.d(">>> NativeRepository ListenNetworkStatus onUnavailable response = $response")
                    JSBridge.onResponse(response, null)
                }
            })
    }

    private fun canBrowseBaidu(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("ping -c 3 www.baidu.com")
            val result = process.waitFor()
            // 0 = 可以访问 ， 1 = 需要认证 ， 2 = 没有网络连接
            result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}