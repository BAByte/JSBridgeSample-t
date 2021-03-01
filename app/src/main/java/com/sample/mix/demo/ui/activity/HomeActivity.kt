package com.sample.mix.demo.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.sample.mix.demo.R
import com.sample.mix.demo.databinding.MainActivityBinding
import com.sample.mix.demo.ui.fragment.MainFragment

class HomeActivity : BaseActivity<MainActivityBinding>() {

    override fun onBinding(savedInstanceState: Bundle?): MainActivityBinding =
        setContentView(this, R.layout.main_activity)

    override fun setupUI(savedInstanceState: Bundle?, binding: MainActivityBinding) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }
    }

    /**
     * 当在主页时屏蔽返回键
     */
    override fun onBackPressed() {
    }
}