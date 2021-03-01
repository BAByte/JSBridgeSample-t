package com.sample.mix.demo.ui.fragment.viewmodel

import android.app.Application
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import com.sample.mix.demo.repository.RepositoryLoader

class JSBridgeModel constructor(val app: Application) : AndroidViewModel(app) {

    @JavascriptInterface
    fun requestFromJS(requestJson: String): String {
        return RepositoryLoader.provideNativeRepository().requestFromJS(requestJson)
    }

    fun initClient(webViewClient: WebView?) {
        RepositoryLoader.provideNativeRepository().initJSBridge(webViewClient)
    }
}