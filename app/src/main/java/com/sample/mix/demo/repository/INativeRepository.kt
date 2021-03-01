package com.sample.mix.demo.repository

import android.webkit.WebView

/**
 * js 调用 java 的本地具体实现
 */
interface INativeRepository {
    fun requestFromJS(requestJson: String): String
    fun initJSBridge(webViewClient: WebView?)
}