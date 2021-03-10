package com.sample.mix.demo.ui.fragment

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.sample.mix.demo.R
import com.sample.mix.demo.databinding.MainFragmentBinding
import com.sample.mix.demo.ui.fragment.viewmodel.JSBridgeModel


class MainFragment : BaseFragment<MainFragmentBinding>() {
    private val viewModel: JSBridgeModel by viewModels()

    override fun onBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): MainFragmentBinding = MainFragmentBinding.inflate(inflater, container, false)


    override fun setupUI(binding: MainFragmentBinding) {
        viewModel.initClient(binding.webRoot)
        //向前端注入对象
        binding.webRoot.addJavascriptInterface(viewModel, "JSBridgeModel")
        //硬件加速会导致滑动花屏
        binding.webRoot.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.webRoot.setBackgroundColor(requireContext().resources.getColor(R.color.color_bg)) // 设置背景色
        val settings: WebSettings = binding.webRoot.settings
        settings.javaScriptEnabled = true

        //cache settings
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setAppCachePath(context?.cacheDir?.absolutePath)
        settings.setAppCacheEnabled(true)
        settings.databaseEnabled = true
        settings.domStorageEnabled = true

        // Enable zooming in web view
        settings.setSupportZoom(false)

        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true // api 26
        }
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.defaultTextEncodingName = "UTF-8"

        /**
         * Webview在安卓5.0之前默认允许其加载混合网络协议内容
         * 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // More optional settings, you can enable it by yourself
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.loadWithOverviewMode = true
        settings.allowContentAccess = true
        settings.setGeolocationEnabled(true)
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccess = true
        settings.domStorageEnabled = true
        settings.supportMultipleWindows()
        settings.allowContentAccess = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadsImagesAutomatically = true

        // WebView settings
        binding.webRoot.fitsSystemWindows = true


        TODO("这里加载前端资源 不部署服务器的话，让前端给静态文件放在asset文件夹，安卓直接加载就好，当然你要放个https://baidu.com,也是可以的")
        binding.webRoot.loadUrl("file:android_asset/index.html")

        binding.webRoot.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                e: SslError?
            ) {
                handler?.proceed()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
