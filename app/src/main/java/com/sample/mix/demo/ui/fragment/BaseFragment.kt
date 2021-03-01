package com.sample.mix.demo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.properties.Delegates

abstract class BaseFragment<BINDING : ViewDataBinding> :
    Fragment(), CoroutineScope by MainScope() {

    var binding: BINDING by Delegates.notNull()

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = onBinding(inflater, container, savedInstanceState).apply {
            lifecycleOwner = this@BaseFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(binding)
        subscribeUI()
    }

    protected abstract fun onBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): BINDING

    protected abstract fun setupUI(binding: BINDING)

    protected open fun subscribeUI() {

    }

    override fun onDestroy() {
        super.onDestroy()
        this.cancel()
    }
}