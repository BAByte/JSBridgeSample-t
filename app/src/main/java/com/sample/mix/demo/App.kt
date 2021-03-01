package com.sample.mix.demo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.sample.mix.demo.repository.RepositoryLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi


class App : Application() {

    companion object {
        const val TAG = "PadIoT"

        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
    }


    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        ctx = this
        initRepository()
    }

    @ExperimentalCoroutinesApi
    private fun initRepository() {
        RepositoryLoader.init(this)
    }
}
