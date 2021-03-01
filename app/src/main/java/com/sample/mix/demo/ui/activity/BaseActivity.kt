package com.sample.mix.demo.ui.activity

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.*
import kotlin.properties.Delegates


abstract class BaseActivity<BINDING : ViewDataBinding> : AppCompatActivity(),
    CoroutineScope by MainScope() {
    private var binding: BINDING by Delegates.notNull()

    companion object {
        val activitys = mutableListOf<AppCompatActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏应用程序的标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //隐藏android系统的状态栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = onBinding(savedInstanceState)
        binding.lifecycleOwner = this@BaseActivity
        setupUI(savedInstanceState, binding)
        activitys.add(this)
    }

    protected abstract fun onBinding(savedInstanceState: Bundle?): BINDING

    protected abstract fun setupUI(savedInstanceState: Bundle?, binding: BINDING)

    override fun onDestroy() {
        super.onDestroy()
        this.cancel()
        activitys.remove(this)
    }
}